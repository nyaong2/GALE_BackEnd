package com.SideProject.GALE.util.kakaoMap;


import java.net.URI;
import java.net.URLEncoder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


public class KakaoMapApi {
	private static final String BASE_URL = "https://dapi.kakao.com/v2/local/search/category.json?category\\\\_group\\\\_code=";
	
	//@Value("${kakaomap.restapi_key}")
	//private String RESTAPI_KEY = "2baf55e9d9fa1ee11a26878f7b8f6240";
	
	private String apiKey = "KakaoAK 2baf55e9d9fa1ee11a26878f7b8f6240";
	
	
	public void category()
	{
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", apiKey);
		
		HttpEntity<String> entity = new HttpEntity<String>("parameters",headers);
		try {
		//String encode = URLEncoder.encode("AT4", "UTF-8"); 
        String rawURI = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("category_group_code", "AT4")
                .queryParam("X", "33.4506287088949")
                .queryParam("Y", "126.5319217424445")
                .queryParam("radius", "100")
                .toUriString();
		URI  uri = new URI(rawURI); 
		
		RestTemplate rest = new RestTemplate();
		ResponseEntity<String> res = rest.exchange(uri, HttpMethod.GET, entity, String.class);
		
		JSONParser jsonParser = new JSONParser(); 
		JSONObject body = (JSONObject) jsonParser.parse(res.getBody().toString()); 
		JSONArray docu = (JSONArray) body.get("documents"); 
		//JSONArray address = (JSONArray) body.get("address"); 
		
		System.out.println(docu.toString());
		
		} catch(Exception ex) {
			System.out.println("test 에러발생");
		}
	}
	
	
	public void address()
	{
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", apiKey);
		
		HttpEntity<String> entity = new HttpEntity<String>("parameters",headers);
		try {
		String encode = URLEncoder.encode("인천광역시 서구 경서동", "UTF-8"); 
		String rawURI = "https://dapi.kakao.com/v2/local/search/address.json?query=" + encode; 
		URI  uri = new URI(rawURI); 
		
		RestTemplate rest = new RestTemplate();
		ResponseEntity<String> res = rest.exchange(uri, HttpMethod.GET, entity, String.class);
		
		JSONParser jsonParser = new JSONParser(); 
		JSONObject body = (JSONObject) jsonParser.parse(res.getBody().toString()); 
		JSONArray docu = (JSONArray) body.get("documents"); 
		//JSONArray address = (JSONArray) body.get("address"); 
		
		System.out.println(docu.toString());
		
		} catch(Exception ex) {
			System.out.println("test 에러발생");
		}
	}
}
