package grades;

import com.ui4j.api.browser.BrowserEngine;
import com.ui4j.api.browser.BrowserFactory;
import com.ui4j.api.browser.Page;
import com.ui4j.api.dom.Document;
import com.ui4j.api.dom.Element;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by frede on 2016-12-21.
 */
public class Start{
    static long rate = 30000;
    static HashMap<Integer, String> gradeMap = new HashMap<>();
    static String phoneNumber = "15149108628";

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {

            if(args.length>0){
                rate = Long.parseLong(args[0]);
                if(args.length > 1){
                    phoneNumber = args[1];
                }
            }

            //get browser and load page
            BrowserEngine browser = BrowserFactory.getWebKit();
            Page page = browser.navigate("http://my.concordia.ca");
            page.show(true);

            // Start a new thread so it does not interrupt the javafx thread
            new Thread(()->{
                //wait for user to login
                while(!page.getDocument().getTitle().toString().contains("Employee-facing")){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //Go to grade page
                page.getWindow().setLocation("https://campus.concordia.ca/psp/pscsprd/EMPLOYEE/HRMS/c/SA_LEARNER_SERVICES.SSR_SSENRL_GRADE.GBL");

                //check grades every 30 seconds
                while(true){
                    promptSemester(page);
                    try {
                        Thread.sleep(rate);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            }).start();
            System.out.println();
    }

    /**
     * Gets the grades for a semester
     * @param page
     */
    public static void promptSemester(Page page){
        Document doc = page.getDocument();
        Document gradeFrame;
        while(true) {
            try {
                gradeFrame = doc.query("#ptifrmtgtframe").get().getContentDocument().get();
                break;
            }catch(Exception e){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        gradeFrame.query("#DERIVED_SSS_SCT_SSS_TERM_LINK").get().click();

        while(true) {
            try {
                gradeFrame.query(".PSRADIOBUTTON[value=\"1\"]").get().click();
                break;
            }catch(Exception e){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        gradeFrame.query("a[name=\"DERIVED_SSS_SCT_SSR_PB_GO\"]").get().click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int index = 0;
        List<String> classNames = new ArrayList();
        gradeFrame.queryAll("span.PSHYPERLINK a").forEach((el)->{
            classNames.add(el.getInnerHTML());
        });

        List<Element> els = gradeFrame.queryAll("span.PABOLDTEXT");



        for(Element el:els){
                if(el.getInnerHTML().contains("Concordia University")){
                    continue;
                }
                if(gradeMap.containsKey(index)){
                    if(!gradeMap.get(index).trim().equalsIgnoreCase(el.getInnerHTML().trim())){
                        newGrade(index,classNames.get(index), el.getInnerHTML());
                    }
                }else{
                    gradeMap.put(index, el.getInnerHTML());
                    newGrade(index,classNames.get(index),el.getInnerHTML());
                }
                index++;
        }

    }

    /**
     * Called when a new grade comes in
     * @param index
     * @param inner
     */
    static void newGrade(int index, String name, String inner){
        System.out.println("Grade for "+name+" is "+inner);
        if(!inner.contains("nbsp")){
            try {

                System.out.println(
                        get("https://rest.nexmo.com/sms/json?api_key="+"df448f61"
                                +"&api_secret="+"1996Concordia"
                                +"&to="+phoneNumber
                                +"&text="+"Grade+for+"+name+"+is+"+inner
                                +"&from="+"14318001814"));
                // handle response here...
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        gradeMap.put(index, inner);
    }
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
    public static String get(String urlStr) throws IOException {
        URL url = new URL(urlStr.replaceAll(" ", "%20"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.addRequestProperty("User-agent", "winterdev_io v0.1");

        conn.setRequestMethod("GET");
        conn.setUseCaches(false);

        conn.connect();

        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
        String jsonStr = readAll(rd);

        return jsonStr;

    }

}
