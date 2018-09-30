package entity;

import com.google.gson.annotations.Expose;
import lombok.Data;
import org.nutz.dao.entity.annotation.Column;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class PttCommon {

    @Expose(serialize = false)
    private final Pattern authorNickNamePattern = Pattern.compile("(\\(.*\\))");

    @Expose
    @Column
    private String authorId;

    @Expose
    @Column
    private String authorName;

    @Expose
    @Column
    private String content;

    @Expose
    @Column
    private LocalDateTime createTime;

    @Expose
    @Column
    private String pttCreateTime;

    public void setSplitAuthorInfo(){
        if(this.authorId != null){
            Matcher matcher = authorNickNamePattern.matcher(this.authorId);
            while (matcher.find()){
                this.authorName = removeStringTarget(matcher.group(1), Arrays.asList("(",")"));
            }
            if(authorId.split(" ").length > 0){
                this.authorId = this.authorId.split(" ")[0];
            }
        }
    }

    private String removeStringTarget(String text,List<String> targets){
        for (String target : targets) {
            text = text.replace(target,"");
        }
        return text;
    }
}
