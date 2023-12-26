package demo.api.vectorDB.service;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tencent.tcvectordb.client.VectorDBClient;
import com.tencent.tcvectordb.model.AIDatabase;
import com.tencent.tcvectordb.model.Collection;
import com.tencent.tcvectordb.model.CollectionView;
import com.tencent.tcvectordb.model.Database;
import com.tencent.tcvectordb.model.DocumentSet;
import com.tencent.tcvectordb.model.param.dml.SearchByContentsParam.Builder;
import com.tencent.tcvectordb.model.param.collectionView.LoadAndSplitTextParam;
import com.tencent.tcvectordb.model.param.database.ConnectParam;
import com.tencent.tcvectordb.model.param.dml.CollectionViewConditionParam;
import com.tencent.tcvectordb.model.param.dml.Filter;
import com.tencent.tcvectordb.model.param.dml.SearchByContentsParam;
import com.tencent.tcvectordb.model.param.dml.SearchOption;
import com.tencent.tcvectordb.model.param.entity.AffectRes;
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

    private AIDatabase db;
    private CollectionView collection;

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

    private void initDB() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withUrl("http://lb-51myby0b-qhlfi29cxdrz7oty.clb.ap-beijing.tencentclb.com:40000")
                .withUsername("root")
                .withKey("pSmKvZHseHE5NiRKPZo4FuupGmADHrWl6xtFSPPB")
                .withTimeout(30)
                .build();
        setClient(new VectorDBClient(connectParam, ReadConsistencyEnum.EVENTUAL_CONSISTENCY));

        this.db = this.client.aiDatabase(DB_NAME);
        this.collection = this.db.describeCollectionView(COLLECTION_NAME);
        System.out.println("初始化db");
        System.out.println(this.client.listDatabase());
    }

    @Override
    public List<String> accurateQury(CollectionViewQo collectionViewQo) {
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
    public DocumentSet importFile(ImportFile file) throws Exception {
        Path localPath = remoteUrlToLocalTemp(file.getPath(), file.getFileName());
        LoadAndSplitTextParam param = LoadAndSplitTextParam.newBuilder().withLocalFilePath(localPath.toString())
                .Build();
        // 配置文件 Meatdata 标量字段的值
        Map<String, Object> metaDataMap = new HashMap<>();
        metaDataMap.put("author", file.getAuthor());
        metaDataMap.put("tags", file.getTags());
        // 调用 loadAndSplitText() 上传文件
        try {
            collection.loadAndSplitText(param, metaDataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                Files.deleteIfExists(localPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return getFileByName(file.getFileName());
    }

    @Override
    public DocumentSet updateFile(ImportFile file) throws Exception {
        System.out.println(getFileByName(file.getFileName()));
        if (getFileByName(file.getFileName()) != null) {
            removeFileByName(file.getFileName());
        }
        return importFile(file);
    }

    private Path remoteUrlToLocalTemp(String fileUrl, String fileName) {
        Path tempFile = null;
        Path newFilePath = null;
        try {
            tempFile = Files.createTempFile("tempfile_", ".md");

            // 打开网络输入流
            InputStream in = new URL(fileUrl).openStream();
            BufferedInputStream bufIn = new BufferedInputStream(in);

            // 打开一个输出流到临时文件
            FileOutputStream fout = new FileOutputStream(tempFile.toFile());

            // 缓冲区
            byte[] buffer = new byte[1024];
            int bytesRead;

            // 读取数据并写入临时文件
            while ((bytesRead = bufIn.read(buffer)) != -1) {
                fout.write(buffer, 0, bytesRead);
            }

            // 清理工作
            fout.close();
            bufIn.close();
            // 修改临时文件的文件名
            newFilePath = tempFile.resolveSibling(fileName);
            Files.move(tempFile, newFilePath, StandardCopyOption.REPLACE_EXISTING);

            // 输出新文件名的路径
            System.out.println("Renamed file path: " + newFilePath.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFilePath;
    }

    @Override
    public void removeFileById(String id) {
        // 配置删除条件，以便检索需删除的文件
        CollectionViewConditionParam build = CollectionViewConditionParam
                .newBuilder()
                .withDocumentSetIds(id)
                // .withFilter(new Filter("author=\"maodou\""))
                .build();
        // 删除文件
        AffectRes affectRes = collection.deleteDocumentSets(build);
        // 输出
        System.out.println("\tres: " + affectRes.toString());
        System.out.println(collection.query().size());
    }

    @Override
    public void removeFileByName(String name) {
        // 配置删除条件，以便检索需删除的文件
        CollectionViewConditionParam build = CollectionViewConditionParam
                .newBuilder().withDocumentSetNames(name)
                // .withFilter(new Filter("author=\"maodou\""))
                .build();
        // 删除文件
        AffectRes affectRes = collection.deleteDocumentSets(build);
        // 输出
        System.out.println("\tres: " + affectRes.toString());
        System.out.println(collection.query().size());
    }

    @Override
    public List<DocumentSet> getAllFiles() {
        return collection.query();
    }

    @Override
    public DocumentSet getFileByName(String fileName) {
        try {
            return collection.getDocumentSetByName(fileName);
        } catch (Exception e) {
            return null;
        }
    }

    public DocumentSet getFileById(String id) {
        try {
            return collection.getDocumentSetById(id);
        } catch (Exception e) {
            return null;
        }
    }
};