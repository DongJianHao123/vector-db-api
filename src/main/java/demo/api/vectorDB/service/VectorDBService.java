package demo.api.vectorDB.service;

import java.util.List;

import com.tencent.tcvectordb.client.VectorDBClient;
import com.tencent.tcvectordb.model.DocumentSet;

import demo.api.vectorDB.entity.CollectionViewQo;
import demo.api.vectorDB.entity.ImportFile;

public interface VectorDBService {
    List<String> accurateQuery(CollectionViewQo collectionViewQo);

    VectorDBClient getClient();

    DocumentSet importFile(ImportFile file) throws Exception;

    DocumentSet updateFile(ImportFile file) throws Exception;

    void removeFileById(String id);

    void removeFileByName(String name);

    List<DocumentSet> getAllFiles();

    DocumentSet getFileByName(String fileName);
}