package demo.api.vectorDB.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tencent.tcvectordb.client.VectorDBClient;
import com.tencent.tcvectordb.model.AIDatabase;
import com.tencent.tcvectordb.model.CollectionView;
import com.tencent.tcvectordb.model.param.dml.SearchByContentsParam.Builder;
import com.tencent.tcvectordb.model.param.collectionView.LoadAndSplitTextParam;
import com.tencent.tcvectordb.model.param.database.ConnectParam;
import com.tencent.tcvectordb.model.param.dml.SearchByContentsParam;
import com.tencent.tcvectordb.model.param.dml.SearchOption;
import com.tencent.tcvectordb.model.param.entity.ContentInfo;
import com.tencent.tcvectordb.model.param.entity.SearchContentInfo;
import com.tencent.tcvectordb.model.param.enums.ReadConsistencyEnum;

import demo.api.vectorDB.entity.CollectionViewQo;
import demo.api.vectorDB.entity.ImportFile;

@Service
public class VectorDBServiceImpl implements VectorDBService {

    private VectorDBClient client;

    private static String DB_NAME = "maodou";
    private static String COLLECTION_NAME = "docs";

    public VectorDBServiceImpl() {
        initDB();
    }

    @Override
    public VectorDBClient getClient() {
        return client;
    }

    public void setClient(VectorDBClient client) {
        this.client = client;
    }

    public void initDB() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withUrl("http://lb-51myby0b-qhlfi29cxdrz7oty.clb.ap-beijing.tencentclb.com:40000")
                .withUsername("root")
                .withKey("pSmKvZHseHE5NiRKPZo4FuupGmADHrWl6xtFSPPB")
                .withTimeout(30)
                .build();
        setClient(new VectorDBClient(connectParam, ReadConsistencyEnum.EVENTUAL_CONSISTENCY));
        System.out.println("初始化db");
        System.out.println(this.client.listDatabase());
    }

    @Override
    public List<String> accurateQury(CollectionViewQo collectionViewQo) {
        AIDatabase database = client.aiDatabase(DB_NAME);
        CollectionView collection = database.describeCollectionView(COLLECTION_NAME);

        String content = collectionViewQo.getContent();
        String documentName = collectionViewQo.getDocumentName();
        Integer limit = collectionViewQo.getLimit();
        Builder paramBuilder = SearchByContentsParam.newBuilder()
                .withSearchContentOption(SearchOption.newBuilder().withChunkExpand(Arrays.asList(1, 1)).build());

        if (content != "" && content != null) {
            paramBuilder.withContent(collectionViewQo.getContent());
        }
        if (documentName != null && documentName != "") {
            paramBuilder.withDocumentSetName(documentName);
        }
        if (limit == null || limit <= 0) {
            limit = 3;
        }

        paramBuilder.withLimit(collectionViewQo.getLimit());
        SearchByContentsParam searchByContentsParam = paramBuilder.build();

        List<SearchContentInfo> searchContentInfos = collection.search(searchByContentsParam);
        List<String> res = searchContentInfos.stream().map(SearchContentInfo::getData).map(ContentInfo::getText)
                .collect(Collectors.toList());

        return res;
    }

    @Override
    public void importFile(ImportFile file) {
        AIDatabase db = client.aiDatabase(DB_NAME);
        CollectionView collection = db.describeCollectionView(COLLECTION_NAME);
        LoadAndSplitTextParam param = LoadAndSplitTextParam.newBuilder().withLocalFilePath(file.getPath()).Build();
        // 配置文件 Meatdata 标量字段的值
        Map<String, Object> metaDataMap = new HashMap<>();
        metaDataMap.put("author", file.getAuthor());
        metaDataMap.put("tags", file.getTags());
        // 调用 loadAndSplitText() 上传文件
        try {
            collection.loadAndSplitText(param, metaDataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
};