import com.google.gson.Gson;
import dao.DataSource;
import entity.PttArticle;
import entity.PttComment;
import entity.PttCommon;
import org.nutz.dao.impl.NutDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ExportService {
    private NutDao dao = DataSource.getDao();
    private Logger logger = LoggerFactory.getLogger(ExportService.class);

    public void exportArticle(List<PttArticle> articles) {
        List<PttArticle> insertArticle = articles.parallelStream().filter(article ->
                dao.fetch(PttArticle.class, article.getArticleId()) == null
        ).collect(Collectors.toList());
        List<PttArticle> updateArticle = new ArrayList<>(articles);
        updateArticle.removeAll(insertArticle);

        List<PttComment> comments = new ArrayList<>();
        articles.stream().forEach(article -> comments.addAll(article.getComments()));
        List<PttComment> insertComment = comments.parallelStream().filter(comment -> comment != null).filter(comment ->
                dao.fetchx(PttComment.class, comment.getArticleId(), comment.getCommentIndex()) == null
        ).collect(Collectors.toList());
        List<PttComment> updateComment = new ArrayList<>(comments);
        updateComment.removeAll(insertComment);

        dao.fastInsert(insertArticle);
        dao.update(updateArticle);
        dao.fastInsert(insertComment);
        dao.update(updateComment);
    }


}
