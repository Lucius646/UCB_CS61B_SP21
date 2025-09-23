package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StagingArea implements Serializable {
    //存储待添加的文件名及其哈希值
    private Map<String, String> fieldToAdd;

    //存储待删除的文件名及其哈希值
    private Map<String, String> fieldToRemove;

    //构造函数
    public StagingArea() {
        fieldToAdd = new HashMap<>();
        fieldToRemove = new HashMap<>();
    }

    //公共clear方法，清空缓存区
    public void clear() {
        fieldToAdd.clear();
        fieldToRemove.clear();
    }


    //公共add方法，使用add命令时调用
    public void add(String fileName, String fileHash) {
        fieldToAdd.put(fileName,fileHash);
    }

    //公共remove方法，使用rm命令时调用
    public void remove(String fileName) {
        fieldToRemove.put(fileName, "Removed");
    }

    //返回add缓存区哈希表
    public Map<String, String> getFieldToAdd() {
        return fieldToAdd;
    }

    //返回rm缓存区哈希表
    public Map<String, String> getFieldToRemove() {
        return fieldToRemove;
    }
}
