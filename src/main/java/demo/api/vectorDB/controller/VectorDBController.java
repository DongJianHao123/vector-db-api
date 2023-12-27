package demo.api.vectorDB.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import demo.api.vectorDB.entity.CollectionViewQo;
import demo.api.vectorDB.entity.ImportFile;
import demo.api.vectorDB.service.VectorDBService;

@Controller
@RequestMapping(value = "vector-db")
public class VectorDBController {
    @Autowired
    private VectorDBService vectorDBService;

    @PostMapping(value = "query")
    public ResponseEntity<Object> query(@RequestBody CollectionViewQo collectionViewQo) {
        return ResponseEntity.ok(vectorDBService.accurateQuery(collectionViewQo));
    }

    @GetMapping(value = "get_all_files")
    public ResponseEntity<Object> getAllFiles() {
        return ResponseEntity.ok(vectorDBService.getAllFiles().toString());
    }

    @GetMapping(value = "get_file")
    public ResponseEntity<Object> getFile(@RequestParam String name) {
        return ResponseEntity.ok(vectorDBService.getFileByName(name).toString());
    }

    @PostMapping(value = "import_file")
    public ResponseEntity<Object> importFile(@RequestBody ImportFile file) throws Exception {
        return ResponseEntity.ok(vectorDBService.importFile(file).toString());
    }

    @PostMapping(value = "update_file")
    public ResponseEntity<Object> updateFile(@RequestBody ImportFile file) throws Exception {
        return ResponseEntity.ok(vectorDBService.updateFile(file).toString());
    }

    @PostMapping(value = "remove_file")
    public ResponseEntity<Object> removeFile(@RequestBody ImportFile file) {
        if (file.getId() != null) {
            vectorDBService.removeFileById(file.getId());
        } else if (file.getFileName() != null) {
            vectorDBService.removeFileByName(file.getFileName());
        }
        return ResponseEntity.ok("success");
    }
}
