package ai.example.springai.controller;

import ai.example.springai.service.PptService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/springPpt")
@AllArgsConstructor
public class PptController {

    PptService pptService;

    @RequestMapping("/createPpt")
    public String createPpt(@RequestParam(name = "message") String message){
          String message1 =  pptService.createPpt(message);
          System.out.println(message1);
        return  message1;
    }

}
