import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entity.PttArticle;
import entity.PttComment;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.xsoup.Xsoup;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SearchService {
    private Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final String pttHost = "https://www.ptt.cc/bbs/";
    private final String pttHostForArticle = "https://www.ptt.cc";
    private final Map<String,String> monthMap = new HashMap<>();
    private final int monthIndex = 1;
    private final int dayIndex = 2;
    private final int timeIndex = 3;
    private final int yearIndex = 4;
    private ExportService exportService = new ExportService();

    public SearchService(){
        monthMap.put("Jan","01");
        monthMap.put("Feb","02");
        monthMap.put("Mar","03");
        monthMap.put("Apr","04");
        monthMap.put("May","05");
        monthMap.put("Jun","06");
        monthMap.put("Jul","07");
        monthMap.put("Aug","08");
        monthMap.put("Sep","09");
        monthMap.put("Oct","10");
        monthMap.put("Nov","11");
        monthMap.put("Dec","12");
    }

    public static void main(String[] args){
        SearchService demo = new SearchService();
        demo.exportArticle("Gossiping",38163,-1);
    }

    public void exportArticle(String board,Integer startIndex,Integer sum){
        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(LocalDateTime.class,new LocalDateTimeConverter()).excludeFieldsWithoutExposeAnnotation().create();
        Integer maximumIndex = startIndex == null ? getMaximumIndex(board) : startIndex;
        if(sum==-1){
            while(maximumIndex>=1){
                logger.info(board+" "+maximumIndex+" "+LocalDateTime.now());
                List<PttArticle> result = getArticlePage(board,maximumIndex--);
                new Thread(()->exportService.exportArticle(result)).start();
            }
        }else{
            while(sum>0){
                logger.info(board+" "+maximumIndex+" "+LocalDateTime.now());
                List<PttArticle> result = getArticlePage(board,maximumIndex--);
                new Thread(()->exportService.exportArticle(result)).start();
                sum--;
            }
        }
    }

    public List<PttArticle> getArticlePage(String board, Integer page){
        List<PttArticle> articles = getNewestArticlePageSummaryList(board,page);
        articles.parallelStream().forEach(article->{
            Document articleDoc = getDocument(pttHostForArticle+article.getHref());
            if(articleDoc!=null){
                String authorId = Xsoup.compile("//*[@id=\"main-content\"]/div[1]/span[2]/text()").evaluate(articleDoc).get();
                String content = Xsoup.compile("//*[@id=\"main-content\"]/text()").evaluate(articleDoc).get();
                String createTime = Xsoup.compile("//*[@id=\"main-content\"]/div[4]/span[2]/text()").evaluate(articleDoc).get();
                int commentIndex = 5;
                List<PttComment> comments = new ArrayList<>();
                while(true){
                    String pushTag = Xsoup.compile("//*[@id=\"main-content\"]/div["+commentIndex+"]/span[1]/text()").evaluate(articleDoc).get();
                    String commentUserId = Xsoup.compile("//*[@id=\"main-content\"]/div["+commentIndex+"]/span[2]/text()").evaluate(articleDoc).get();
                    String commentContent = Xsoup.compile("//*[@id=\"main-content\"]/div["+commentIndex+"]/span[3]/text()").evaluate(articleDoc).get();
                    String commentTime = Xsoup.compile("//*[@id=\"main-content\"]/div["+commentIndex+"]/span[4]/text()").evaluate(articleDoc).get();
                    if(commentUserId==null){
                        break;
                    }else{
                        PttComment comment = new PttComment();
                        comment.setArticleId(article.getArticleId());
                        comment.setPushTag(pushTag);
                        comment.setAuthorId(commentUserId);
                        comment.setContent(commentContent);
                        comment.setPttCreateTime(commentTime);
                        comment.setSplitAuthorInfo();
                        comment.setCommentIndex(commentIndex);
                        comments.add(comment);
                        commentIndex++;
                    }
                }
                article.setComments(comments);
                article.setAuthorId(authorId);
                article.setContent(content);
                article.setPttCreateTime(createTime);
                article.setCreateTime(parseToLocalDateTime(createTime));
                article.setSplitAuthorInfo();
            }
        });
        articles.removeIf(article->article.getContent()==null);
        return articles;
    }

    public List<PttArticle> getNewestArticlePageSummaryList(String board,Integer page){
        Document articleListDoc = getArticleListDoc(board,page);
        List<PttArticle> newestArticleList = new LinkedList<>();
        for (int i = 0; i < 20; i++) {
            String articleHref = Xsoup.compile("//*[@id=\"main-container\"]/div[2]/div[" + (i) + "]/div[@class=\"title\"]/a/@href").evaluate(articleListDoc).get();
            String articleTitle = Xsoup.compile("//*[@id=\"main-container\"]/div[2]/div[" + (i) + "]/div[@class=\"title\"]/a/text()").evaluate(articleListDoc).get();
            if (articleTitle != null) {
                PttArticle article = new PttArticle();
                article.setTitle(articleTitle);
                article.setBoard(board);
                article.setHref(articleHref);
                article.setArticleId(articleHref.split("/")[3].replace(".html",""));
                newestArticleList.add(article);
                logger.info("Title "+articleTitle);
            }
        }
        return newestArticleList;
    }

    private LocalDateTime parseToLocalDateTime(String timeStr){
        try{
            String[] timeSplit = timeStr.split(" ");
            timeStr = timeSplit[yearIndex]+"-"+monthMap.get(timeSplit[monthIndex])+"-"+(timeSplit[dayIndex].length()==1 ? 0+timeSplit[dayIndex] : timeSplit[dayIndex])+" "+timeSplit[timeIndex];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(timeStr, formatter);
            return dateTime;
        }catch (Exception e){
            logger.info("parseToLocalDateTime Fail "+timeStr);
            return null;
        }
    }

    private Integer getMaximumIndex(String board){
        Document articleListDoc = getDocument(pttHost+board+"/index.html");
        String newestPage = Xsoup.compile("//*[@id=\"action-bar-container\"]/div/div[2]/a[2]").evaluate(articleListDoc).get();
        return Integer.parseInt(newestPage.split("/")[3].split("\\.")[0].replace("index",""))+1;
    }

    private Document getArticleListDoc(String board,Integer index){
        return getDocument(pttHost+board+"/index"+index+".html");
    }

    private Document getDocument(String url){
        try {
            Thread.sleep(10);
            return Jsoup.connect(url).cookie("over18","1").get();
        }catch (HttpStatusException e){
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            return getDocument(url);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }


}
