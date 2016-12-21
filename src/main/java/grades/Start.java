package grades;

import com.ui4j.api.browser.BrowserEngine;
import com.ui4j.api.browser.BrowserFactory;
import com.ui4j.api.browser.Page;
import com.ui4j.api.dom.Document;
import com.ui4j.api.dom.Element;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;


/**
 * Created by frede on 2016-12-21.
 */
public class Start{

    static HashMap<Integer, String> gradeMap = new HashMap<>();

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {

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
                        Thread.sleep(30000);
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
        List<Element> els = gradeFrame.queryAll("span.PABOLDTEXT");



        for(Element el:els){
                if(gradeMap.containsKey(index)){
                    if(gradeMap.get(index) != el.getInnerHTML()){
                        newGrade(index,el.getInnerHTML());
                    }
                }else{
                    gradeMap.put(index, el.getInnerHTML());
                    newGrade(index,el.getInnerHTML());
                }
                index++;
        }

    }

    /**
     * Called when a new grade comes in
     * @param index
     * @param inner
     */
    static void newGrade(int index, String inner){
        System.out.println("Grade at "+index+" is "+inner);
        gradeMap.put(index, inner);
    }

}
