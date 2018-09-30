package entity;

import com.google.gson.annotations.Expose;
import lombok.Data;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Table;

@Data
@Table("ptt_comment")
@PK( {"articleId","commentIndex"} )
public class PttComment extends PttCommon{

    @Expose
    private String articleId = "";

    @Expose
    private Integer commentIndex = 0;

    @Expose
    @Column
    private String pushTag;

}
