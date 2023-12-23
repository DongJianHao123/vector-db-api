package demo.api.vectorDB.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import demo.api.vectorDB.entity.CollectionViewQo;
import demo.api.vectorDB.entity.ImportFile;
import demo.api.vectorDB.service.VectorDBService;

@Controller
@RequestMapping(value = "vector-db")
public class VectorDBController {
    @Autowired
    private VectorDBService vectorDBService;

    @PostMapping(value = "qury")
    public ResponseEntity<Object> query(@RequestBody CollectionViewQo collectionViewQo) {
        return ResponseEntity.ok(vectorDBService.accurateQury(collectionViewQo));
    }

    @PostMapping(value = "import_file")
    public ResponseEntity<Object> importFile(@RequestBody ImportFile file) {
        System.out.println(file);
        vectorDBService.importFile(file);
        return ResponseEntity.ok("success");
    }
}
