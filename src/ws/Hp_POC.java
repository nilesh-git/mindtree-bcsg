package ws;

import java.util.Calendar;

import javax.ws.rs.core.MediaType;

import org.apache.catalina.util.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ws.response.parser.JsonParser;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class Hp_POC {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JSONObject json = new JSONObject(); //
		// Create a trust manager that does not validate certificate chains
		/*
		 * TrustManager[] trustAllCerts = new TrustManager[] { new
		 * X509TrustManager() { public X509Certificate[] getAcceptedIssuers() {
		 * return null; }
		 * 
		 * public void checkClientTrusted(X509Certificate[] certs, String
		 * authType) { }
		 * 
		 * public void checkServerTrusted(X509Certificate[] certs, String
		 * authType) { } } };
		 * 
		 * // Install the all-trusting trust manager try { SSLContext sc =
		 * SSLContext.getInstance("TLS"); sc.init(null, trustAllCerts, new
		 * SecureRandom()); HttpsURLConnection
		 * .setDefaultSSLSocketFactory(sc.getSocketFactory()); } catch
		 * (Exception e) { ; }
		 */
		Client client = Client.create();
		try {
			JSONObject jsonCred = new JSONObject();
			jsonCred.put("username", "bcsguser");
			jsonCred.put("password", "cloud");
			json.put("passwordCredentials", jsonCred);
			json.put("tenantName", "BCSG");
			System.out.println("Request body: " + json.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		WebResource webResource = client
				.resource("https://csa45.pocaas.hpintelco.org:8444/idm-service/v2.0/tokens");
		// WebResource webResource =
		// client.resource("https://213.30.160.29:8444/idm-service/v2.0/tokens");

		String authorization = "idmTransportUser" + ":" + "idmTransportUser";
		authorization = headerAuth(authorization);

		ClientResponse wsResponse = webResource
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.header("Authorization", authorization)
				.post(ClientResponse.class, json);
		try {
			JSONObject result = wsResponse.getEntity(JSONObject.class);
			JSONObject token = result.getJSONObject("token");
			String token_id = token.getString("id");
			System.out.println("Token ID: " + token_id);
			listSvcOfferings(token_id, "");
		} catch (Exception e) {
			System.out.println("Exception message: " + e.getMessage());
		}

	}

	public static String headerAuth(String credentials) {
		if (credentials != null) {
			String encoded = Base64.encode(credentials.getBytes());
			credentials = "Basic " + encoded;
		}
		return credentials;
	}

	public static void listSvcOfferings(String token, String filter) {
		JSONObject json = new JSONObject();
		Client client = Client.create();
		try {
			json.put("approval", "ALL");
			System.out.println("Request body: " + json.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		WebResource webResource = client
				.resource("https://csa45.pocaas.hpintelco.org:8444/csa/api/mpp/mpp-offering/filter");
		// WebResource webResource =
		// client.resource("https://213.30.160.29:8444/csa/api/mpp/mpp-offering/filter");
		String authorization = "idmTransportUser" + ":" + "idmTransportUser";
		authorization = headerAuth(authorization);
		ClientResponse wsResponse = webResource
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.header("Authorization", authorization)
				.header("X-Auth-token", token).post(ClientResponse.class, json);
		try {
			JSONObject result = wsResponse.getEntity(JSONObject.class);
			JSONArray members = JsonParser.parseJson(result);
			System.out.println("Service offerings: " + result.toString());
			getOfferingDetails(token,members,"SIMPLE_SYSTEM");
		} catch (Exception e) {
			System.out.println("Exception message: " + e.getMessage());
			System.out.println("Response message:" + wsResponse.toString());
		}
	}

	public static void getOfferingDetails(String token,JSONArray members, String category){
		StringBuilder uri = new StringBuilder();
		uri.append("https://csa45.pocaas.hpintelco.org:8444/csa/api/mpp/mpp-offering/");//<svcOfferingId>?catalogId=<ctId>&category=<ctgry>");
		for(int i=0;i<=members.length();i++){
			try {
				JSONObject memBr = (JSONObject) members.get(i);
				JSONObject memBrCategory =	(JSONObject) memBr.get("category");
				if(category.equalsIgnoreCase((String)memBrCategory.get("name"))){
					uri.append(memBr.get("id"));
					uri.append("?catalogId="+memBr.get("catalogId")+"&category=");
					uri.append(category);
					System.out.println("URI: "+uri.toString());
					break;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Client client = Client.create();
		WebResource webResource = client
				.resource(uri.toString());
		String authorization = "idmTransportUser" + ":" + "idmTransportUser";
		authorization = headerAuth(authorization);
		ClientResponse wsResponse = webResource
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.header("Authorization", authorization)
				.header("X-Auth-token", token).get(ClientResponse.class);
		try {
			JSONObject result = wsResponse.getEntity(JSONObject.class);
			System.out.println("Offerings Details: " + result.toString());
			createSubscription(token,"SIMPLE_SYSTEM");
		} catch (Exception e) {
			System.out.println("Exception message: " + e.getMessage());
			System.out.println("Response message:" + wsResponse.toString());
		}
	}

	public static void createSubscription(String token,String categoryName){
		JSONObject json = new JSONObject(); 
		Client client = Client.create();
		StringBuilder s = new StringBuilder();
		s.append("--Abcdefgh ");
		s.append("\n Content-Disposition: form-data; name=\"requestForm\" ");
		try {
			json.put("categoryName", "SOFTWARE");
			json.put("subscriptionName", "NEW_TEST");
			json.put("startDate", "2015-08-21T05:32:17.000Z");
			Calendar c = Calendar.getInstance();
					c.add(Calendar.MONTH, 1);
			json.put("endDate", "2015-09-24T05:32:17.000Z");
			json.put("fields", "");
			json.put("action", "ORDER");
			s.append(json.toString()+"\n --Abcdefgh--");
			System.out.println("Request body: " + s.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		WebResource webResource = client
				.resource("https://csa45.pocaas.hpintelco.org:8444/csa/api/mpp/mpp-request/8a83d7a44f1d0f21014f1d37683608a1?catalogId=8a83d7a44f1d0f21014f23000f8816df&category=SOFTWARE");
		String authorization = "idmTransportUser" + ":" + "idmTransportUser";
		authorization = headerAuth(authorization);
		ClientResponse wsResponse = webResource
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.MULTIPART_FORM_DATA+";boundary=Abcdefgh")
				.header("Authorization", authorization)
				.header("X-Auth-token", token).post(ClientResponse.class,s.toString());
		try {
			JSONObject result = wsResponse.getEntity(JSONObject.class);
			System.out.println("Offerings Details: " + result.toString());
		} catch (Exception e) {
			System.out.println("Exception message: " + e.getMessage());
			System.out.println("Response message:" + wsResponse.toString());
		}
	}

}
