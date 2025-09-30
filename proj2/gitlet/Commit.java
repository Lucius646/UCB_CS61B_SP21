package gitlet;


import java.io.Serializable;
import java.util.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Lucius
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String message;

    /** 时间戳*/
    private final Date timeStamp;

    /** 父commit的哈希值列表，merge commit时会有多个父commit*/
    private final List<String> parents;

    /** 存储本次commit跟踪的文件及其blob的映射
     *  key为文件名
     *  value文件为对应的blob的哈希值
     *  */
    private final Map<String, String> trackedFiles;

    /** commit的构造函数*/
    public Commit(String message, List<String> parents, Map<String, String> trackedFiles, Date timeStamp) {
        this.message = message;
        this.parents = parents;
        this.trackedFiles = trackedFiles;
        this.timeStamp = timeStamp;
    }

    /** commit的无参构造 */
    public Commit() {
        this.message = null;
        this.parents = new ArrayList<>();
        this.trackedFiles = new HashMap<String, String>();
        this.timeStamp = new Date();
    }


    /** 提供公共方法*/
    public String getMessage() {
        return message;
    }
    public List<String> getParents() {
        return parents;
    }
    public Date getTimeStamp() {
        return timeStamp;
    }
    public Map<String, String> getTrackedFiles() {
        return trackedFiles;
    }

}
