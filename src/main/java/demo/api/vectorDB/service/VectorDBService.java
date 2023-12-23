package demo.api.vectorDB.service;

import java.util.List;

import com.tencent.tcvectordb.client.VectorDBClient;

import demo.api.vectorDB.entity.CollectionViewQo;
import demo.api.vectorDB.entity.ImportFile;

public interface VectorDBService {
    List<String> accurateQury(CollectionViewQo collectionViewQo);

    VectorDBClient getClient();

    void importFile(ImportFile path);
}