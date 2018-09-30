package entity;

import com.google.gson.annotations.Expose;
import lombok.Data;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Table;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@Table("ptt_article")
public class PttArticle extends PttCommon {

    @Expose
    @Column
    private String title;

    @Expose
    @Name
    private String articleId;


    @Expose
    @Column
    private String board;

    @Expose
    @Column
    private String href;


    @Expose
    private List<PttComment> comments = Collections.emptyList();


}
