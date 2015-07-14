package com.iconectiv.common.controller;

import com.iconectiv.common.utils.Constants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Created by ramp on 7/5/2015.
 * <p/>
 * By default, this controller returns JSP name in WEB-INF/jsp
 * <p/>
 * To return JSON, the controller needs to map the response to response body.
 */
@Controller
public class MainController implements Constants {

    @Autowired
    Config config;

    @RequestMapping("/")
    public String indexHtml(ModelAndView modelAndView) {
        return "redirect:/resources/index.html";
    }

    @RequestMapping("/jsptest")
    public String jsptTest(ModelAndView modelAndView) {
        System.out.println("Query : " + config.getQueryFiles());
        System.out.println("version : " + config.getVersion());
        return "jsp-spring-boot";
    }


    @RequestMapping(value = "/nodesInfo", method = RequestMethod.GET)
    public
    @ResponseBody
    String clustrInfo() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity("http://10.107.1.77:9200" + REST_CMD_NODES, String.class);
        if (HttpStatus.OK == response.getStatusCode()) {
            return response.getBody();
        }
        return "{\"status:\" + response.getStatusCode() + ";
    }

    @RequestMapping(value = "/clusterHealth", method = RequestMethod.GET)
    public
    @ResponseBody
    String clusterHealth() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity("http://10.107.1.77:9200" + REST_CMD_CLUSTER_HEALTH, String.class);
        if (HttpStatus.OK == response.getStatusCode()) {
            return response.getBody();
        }
        return "{\"status:\" + response.getStatusCode() + ";
    }


    @RequestMapping(value = "/indicesInfo", method = RequestMethod.GET)
    public
    @ResponseBody
    String indicesInfo() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity("http://10.107.1.72:9200/_cat/indices", String.class);
        if (HttpStatus.OK == response.getStatusCode()) {
            String indicesListAsStr = response.getBody();
            String[] lines = indicesListAsStr.split("\n");
            JSONArray jsonArray = new JSONArray();
            String plus = Pattern.quote("\\s+");
            for (int i = 0; i < lines.length; i++) {
                String[] tmpElements = lines[i].replaceAll(plus, "").split(" ");
                String[] elements = new String[10];
                int indx = 0;
                for (int k = 0; k < tmpElements.length && indx < 10; k++) {
                    if (!("").equals(tmpElements[k])) {
                        elements[indx] = tmpElements[k];
                        indx++;
                    }
                }
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("health", elements[0]);
                    jsonObject.put("status", elements[1]);
                    jsonObject.put("index", elements[2]);
                    jsonObject.put("primaryreplicas", elements[3] + "/" + elements[4]);
                    jsonObject.put("docsCount", Integer.parseInt(elements[5]));
                    jsonObject.put("size", elements[7]);
                    jsonObject.put("size", elements[8]);
                    jsonArray.put(jsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "{\"status\":\"error parsing\"}";
                }
            }
            return jsonArray.toString();
        }
        return "ok";
    }
}



