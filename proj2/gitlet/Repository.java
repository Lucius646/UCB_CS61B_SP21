package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** objects 目录，存放 commits 和 blobs */
    public static final File OBJECTS_DIR = new File(GITLET_DIR, "objects");

    /** refs 目录，存放分支等引用 */
    public static final File REFS_DIR = new File(GITLET_DIR, "refs");

    /** heads 目录，存放所有分支的最新 commit hash
     * heads目录在refs目录下
     * */
    public static final File HEADS_DIR = new File(REFS_DIR, "heads");

    /** HEAD 文件，指向当前所在的分支 */
    public static final File HEAD_FILE = new File(GITLET_DIR, "HEAD");

    /** StagingArea 文件，存放暂存区对象 */
    public static File STAGING_FILE = join(GITLET_DIR, "staging");//创建路径

    /** Initialize a new repository in the current working directory. */
    //init命令
    public static void init() {
        //1.检查.gitlet是否创建
        if (GITLET_DIR.exists()) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
        if (!GITLET_DIR.mkdir()) {
            throw new GitletException("Failed to create .gitlet directory.");
        }
        // Additional directories and initial state will be created in later steps.

        //2.创建.gitlet的下属文件夹
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();

        //3.创建 initial commit
        Commit initCommit = new Commit("initial commit", new ArrayList<>(), new HashMap<>(), new Date());

        //4.将initCommit序列化并保存
        saveCommit(initCommit);

        //5.创建master分支
        String initCommitHash = sha1(serialize(initCommit));
        File masterBranchFile = join(HEADS_DIR, "master");
        writeContents(masterBranchFile, initCommitHash);

        //6.将HEAD指向master分支
        /**此处的 Utils 是 gitlet 包里的，并非 java 自带的*/
        writeContents(HEAD_FILE, "ref: refs/heads/master");

        //7.创建空暂存区并持久化写入磁盘
        StagingArea stagingArea = new StagingArea();//创建对象
        saveStagingArea(stagingArea);//写入磁盘
    }

    //add命令
    public static void add(String fileName) {
        //1.检查有无初始化
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        //2.定位文件
        File fileToAdd = new File(CWD, fileName);

        //3.检查文件存在与否
        if (!fileToAdd.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        //4.读取文件并序列化，计算哈希值
        byte[] fileContent = readContents(fileToAdd); //序列化对象
        String fileHash = sha1(fileContent); //文件哈希值

        //5.读取暂存区对象
        StagingArea stagingArea = readStagingArea();

        //6.加载HEAD commit，用于判断文件相对于最新commit有无修改
        Commit curCommit = getHeadCommit();
        Map<String, String> trackedFiles = curCommit.getTrackedFiles(); //取得当前commit记录blobs的哈希表

        //7.判断该文件有无被rm
        if (stagingArea.getFieldToRemove().containsKey(fileName)) {
            stagingArea.getFieldToRemove().remove(fileName); // 将文件从RM哈希表中移出
            saveStagingArea(stagingArea);//保存缓存区
            return;
        }

        //8.判断该文件有无修改
        if (trackedFiles.containsKey(fileName) && trackedFiles.get(fileName).equals(fileHash)) {
            //上一层if返回true，说明文件没有修改，若该文件已进入add缓存区，移除它
            if (stagingArea.getFieldToAdd().containsKey(fileName)) {
                stagingArea.getFieldToAdd().remove(fileName); //移除此文件
            }
            saveStagingArea(stagingArea);//保存
            return;
        }

        //9.文件是新文件或被修改
        File blobFile = new File(OBJECTS_DIR, fileHash); //文件存储路径
        writeContents(blobFile, fileContent); //将序列化对象持续化为文件

        //10.更新缓存区
        stagingArea.add(fileName, fileHash);

        //11.保存
        writeObject(STAGING_FILE, stagingArea);
    }

    public static void commit(String message) {
        //1.检查message是否为空
        if (message == null || message.isEmpty()) {
            System.out.println("Please enter a commit message");
            System.exit(0);
        }

        //2.检查缓存区是否为空
        StagingArea stageArea = readObject(STAGING_FILE, StagingArea.class);
        if (stageArea.getFieldToAdd().isEmpty() && stageArea.getFieldToRemove().isEmpty()) {
            System.out.println("No changes add to the commit");
            System.exit(0);
        }

        //3.获取父commit
        String parentCommitHash = getHeadCommitHash();
        Commit parentCommit = getHeadCommit();
        //由于Commit类使用List存储父commit，要将父commit包装为List，即使只有一个父commit
        List<String> parentList = new ArrayList<>();
        parentList.add(parentCommitHash);

        //4.计算新的Commit文件跟踪列表
        Map<String, String> newTrackedFiles = new HashMap<>(parentCommit.getTrackedFiles()); //先传入父commit的文件跟踪列表

        //5.处理暂存区中待添加的文件
        for (Map.Entry<String, String> entry : stageArea.getFieldToAdd().entrySet()) {
            String fileName = entry.getKey();
            String fileHash = entry.getValue();
            newTrackedFiles.put(fileName, fileHash);
        }

        //6.处理暂存区中待删除文件
        for (String fileName : stageArea.getFieldToRemove().keySet()) {
            newTrackedFiles.remove(fileName);
        }

        //7.创建新的的commit对象
        Commit newCommit = new Commit(message, parentList, newTrackedFiles, new Date());

        //9.保存新commit对象
        saveCommit(newCommit);

        //10.更新HEAD指针
        updateCurBranchHead(sha1(serialize(newCommit)));

        //11.清空缓存区
        stageArea.clear();
        saveStagingArea(stageArea);//保存缓存区
    }

    public static void rm(String fileName) {
        //1.检查初始化
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in a initialized Gitlet directory");
            System.exit(0);
        }

        //2.加载缓存区和最新commit对象
        StagingArea stagingArea = readStagingArea();
        Commit headCommit = getHeadCommit();
        Map<String, String> trackedFiles = headCommit.getTrackedFiles();

        //3判断删除文件状态
        boolean stagedForAdd = stagingArea.getFieldToAdd().containsKey(fileName);
        boolean trackedInCommit = trackedFiles.containsKey(fileName);
        if (!stagedForAdd && !trackedInCommit) {
            System.out.println("No reason to remove the file.");
        }

        //4.若文件在缓存区
        if (stagedForAdd) {
            stagingArea.getFieldToAdd().remove(fileName);
        }

        //5.若commit在跟踪文件，添加文件到rm缓存区
        if (trackedInCommit) {
            stagingArea.remove(fileName);

            //6.从真实文件目录中删除文件
            File fileToRemove = join(CWD, fileName);
            if (fileToRemove.exists()) {
                restrictedDelete(fileToRemove);
            }
        }

        //7.更新缓存区
        saveStagingArea(stagingArea);
    }

    public static void log() {
        //1.检查初始化
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in a initialized directory");
            System.exit(0);
        }

        //2.获取HEAD commit 的hash，不要获取对象，用哈希当索引，更新哈希就可以更新commit而不用再创建一次commit对象
        String curCommitHash = getHeadCommitHash();

        //3.遍历，直到init commit
        while (curCommitHash != null) {
            Commit curCommit = readCommit(curCommitHash);

            //打印消息
            System.out.println("===");
            System.out.println("commit " + curCommitHash);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);

            Date commitTimeStamp = curCommit.getTimeStamp();
            String formattedDate = dateFormat.format(commitTimeStamp);
            System.out.println("Date: " + formattedDate);
            
            System.out.println(curCommit.getMessage());
            System.out.println();

            //移动到父commit
            if (curCommit.getParents() != null && !curCommit.getParents().isEmpty()) {
                curCommitHash = curCommit.getParents().get(0);
            } else {
                curCommitHash = null; // 到达 initial commit，它的 parent 列表是空的，循环结束
            }

        }
    }

    public static void globalLog() {
        //1.自检
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        //2.获取objects目录下所以文件名
        List<String> objectHashes = plainFilenamesIn(OBJECTS_DIR);
        if (objectHashes == null) {
            return;
        }

        //3.遍历Object文件，判断是commit还是blob
        for (String commitHash : objectHashes) {
            File objectFile = join(OBJECTS_DIR, commitHash);

            //假设是commit对象
            try {
                Commit curCommit = readObject(objectFile, Commit.class);
                System.out.println("==");
                System.out.println("commit" + commitHash);
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:MM:ss yyyy", Locale.US);
                TimeZone chinaTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
                dateFormat.setTimeZone(chinaTimeZone);
                Date commitTimeStamp = curCommit.getTimeStamp();
                String formattedDate = dateFormat.format(commitTimeStamp);
                System.out.println("Date: " + formattedDate);
                System.out.println(curCommit.getMessage());
                System.out.println();
            } catch (IllegalArgumentException e) {
                //不是Commit就跳过
            }
        }
    }

    public static void find(String message) {
        //1.自检
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        //2.获取objects目录下所以文件名
        List<String> objectHashes = plainFilenamesIn(OBJECTS_DIR);
        if (objectHashes == null) {
            System.out.println("Found no commits with that message.");
            return;
        }

        //3.遍历commit
        boolean findMatch = false;
        for (String commitHash : objectHashes) {
            File objectFile = join(OBJECTS_DIR, commitHash);

            try {
                Commit curCommit = readObject(objectFile, Commit.class);

                //检查message信息是否一致
                if (curCommit.getMessage().equals(message)) {
                    System.out.println(commitHash);
                    findMatch = true;
                }
            } catch (IllegalArgumentException e) {
                //忽略blob
            }
        }

        //4.遍历结束后，若未找到
        if (!findMatch) {
            System.out.println("Found no commits with that message.");
        }
    }

    public static void status() {
        //1.检查初始化
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in a initialized Gitlet directory");
            System.exit(0);
        }

        //2.打印branches
        System.out.println("=== Branches ===");
        //当前分支
        String curBranch = readContentsAsString(HEAD_FILE).replace("ref: refs/heads/", "");
        //所以分支
        List<String> allBranches = plainFilenamesIn(HEADS_DIR);
        //排序打印 ？？
        Collections.sort(allBranches);
        for (String branch : allBranches) {
            if (branch.equals(curBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();

        //3.打印add暂存区
        System.out.println("=== Staged Files ===");
        StagingArea stagingArea = readStagingArea();
        List<String> stagedFiles = new ArrayList<>(stagingArea.getFieldToAdd().keySet());
        Collections.sort(stagedFiles);
        for (String fileName : stagedFiles) {
            System.out.println(fileName);
        }
        System.out.println();

        //4.打印rm缓存区
        System.out.println("=== Removed Files ===");
        List<String> removedFiles = new ArrayList<>(stagingArea.getFieldToRemove().keySet());
        Collections.sort(removedFiles);
        for (String fileName : removedFiles) {
            System.out.println(fileName);
        }
        System.out.println();

        //第五和第六部分需要同时比较工作目录文件、最新commit、暂存区
        //Untracked Files
        //Modifications Not Staged For Commit部分，文件存在于工作目录中，但不在 add 暂存区，也不在当前 commit 的跟踪列表里
        //5.得到所以需要检查的文件，最新commit跟踪的文件 + 当前工作目录文件 + 暂存区文件（？
        Map<String, String> trackedFiles = getHeadCommit().getTrackedFiles();
        Map<String, String> stagedAddFiles = stagingArea.getFieldToAdd();
        Set<String> stagedRemoveFiles = stagingArea.getFieldToRemove().keySet();
        List<String> cwdFileNames = plainFilenamesIn(CWD);

        // 如果工作目录为空，则设为空列表，避免 NullPointerException
        if (cwdFileNames == null) {
            cwdFileNames = new ArrayList<>();
        }
        // 6. 计算 "Modifications Not Staged For Commit"
        List<String> modifiedNotStagedFiles = new ArrayList<>();

        // 遍历所有被跟踪或已暂存的文件，检查它们与工作区的差异
        Set<String> filesToCheck = new HashSet<>(trackedFiles.keySet());
        filesToCheck.addAll(stagedAddFiles.keySet());

        for (String fileName : filesToCheck) {
            File file = join(CWD, fileName);
            boolean inCwd = file.exists();
            String stagedHash = stagedAddFiles.get(fileName);
            String trackedHash = trackedFiles.get(fileName);

            if (inCwd) { // 文件存在于工作区
                String cwdHash = sha1(readContents(file));
                // 情况1: 文件已暂存，但工作区版本又被修改
                if (stagedHash != null && !stagedHash.equals(cwdHash)) {
                    modifiedNotStagedFiles.add(fileName + " (modified)");
                }
                // 情况2: 文件未暂存，但被跟踪，且工作区版本被修改
                else if (stagedHash == null && trackedHash != null && !trackedHash.equals(cwdHash)) {
                    modifiedNotStagedFiles.add(fileName + " (modified)");
                }
            } else { // 文件不存在于工作区
                // 情况3: 文件已暂存，但被从工作区删除
                if (stagedHash != null) {
                    modifiedNotStagedFiles.add(fileName + " (deleted)");
                }
                // 情况4: 文件被跟踪，未被暂存删除，但被从工作区删除
                else if (trackedHash != null && !stagedRemoveFiles.contains(fileName)) {
                    modifiedNotStagedFiles.add(fileName + " (deleted)");
                }
            }
        }

        // 7. 计算 "Untracked Files"
        List<String> untrackedFiles = new ArrayList<>();
        for (String fileName : cwdFileNames) {
            boolean isTracked = trackedFiles.containsKey(fileName);
            boolean isStagedForAdd = stagedAddFiles.containsKey(fileName);

            // 未跟踪的定义：存在于工作目录，但既不被跟踪，也未被暂存添加
            if (!isTracked && !isStagedForAdd) {
                untrackedFiles.add(fileName);
            }
        }

        // 8. 排序并打印
        Collections.sort(modifiedNotStagedFiles);
        Collections.sort(untrackedFiles);

        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String fileName : modifiedNotStagedFiles) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        for (String fileName : untrackedFiles) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    //以下三条命令均是check的子情况

    //用HEAD commit中的文件版本覆盖或添加到工作目录
    public static void checkoutFileFromHead(String fileName) {
        //0.初始化自检
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        Commit headCommit = getHeadCommit();
        Map<String, String> trackedFiles = headCommit.getTrackedFiles();

        //1.检查文件是否在HEAD commit中
        if (!trackedFiles.containsKey(fileName)) {
            System.out.println("File does not exist in that commit");
            return;
        }

        //2.获取该文件在HEAD commit中的hash
        String blobHash = trackedFiles.get(fileName);

        //3.读取blob内容
        File blobFile = join(OBJECTS_DIR, blobHash);
        byte[] fileContent = readContents(blobFile);

        //4.覆盖
        File workingDirectoryFile = join(CWD, fileName);
        writeContents(workingDirectoryFile, fileContent);
    }

    //用指定commit中的文件版本覆盖CWD中的同名文件
    public static void checkoutFileFromCommit(String commitID, String fileName) {
        //0.初始化自检
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        //1.找到ID对应的commit
        File commitFile = join(OBJECTS_DIR, commitID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists");
            return;
        }
        Commit targetCommit = readCommit(commitID);

        //2.检查文件是否在commit中被跟踪
        Map<String, String> trackedFiles = targetCommit.getTrackedFiles();
        if (!trackedFiles.containsKey(fileName)) {
            System.out.println("File does not exist in that commit");
            return;
        }

        //3.覆盖
        String blobHash = trackedFiles.get(fileName);
        File blobFile = join(OBJECTS_DIR, blobHash);
        byte[] fileContent = readContents(blobFile);
        File workingDirectoryFile = join(CWD, fileName);
        writeContents(workingDirectoryFile, fileContent);
    }

    public static void checkoutBranch(String branchName) {
        //0.初始化自检
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        //1.检查目标分支是否存在
        File branchFile = join(HEADS_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists");
            return;
        }

        //2.检查是否需要切换分支
        String curBranch = readContentsAsString(HEAD_FILE).replace("refs: refs/heads/", "");
        if (curBranch.equals(branchName)) {
            System.out.println("No need to checkout the current branch");
            return;
        }

        //3.检查有无untrackedFile会被覆盖(?
        Commit targetCommit = readCommit(readContentsAsString(branchFile));
        Map<String, String> targetTrackedFiles = targetCommit.getTrackedFiles();
        List<String> cwdFiles = plainFilenamesIn(CWD);
        if (cwdFiles != null) {
            Commit headCommit = getHeadCommit();
            Map<String, String> headTrackedFiles = headCommit.getTrackedFiles();

            for (String cwdFile : cwdFiles) {
                if (!headTrackedFiles.containsKey(cwdFile) && targetTrackedFiles.containsKey(cwdFile)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first");
                    return;
                }
            }
        }

        //4.执行转换
        for (Map.Entry<String, String> entry : targetTrackedFiles.entrySet()) {
            String fileName = entry.getKey();
            String blobHash = entry.getValue();
            File blobFile = join(OBJECTS_DIR, blobHash);
            byte[] content = readContents(blobFile);
            File workingDirectoryFile = join(CWD, fileName);
            writeContents(workingDirectoryFile, content);
        }

        //5.删除只在当前分支而不在targetCommit中的文件
        Commit headCommit = getHeadCommit();
        for (String fileName : headCommit.getTrackedFiles().keySet()) {
            if (!targetTrackedFiles.containsKey(fileName)) {
                restrictedDelete(join(CWD, fileName));
            }
        }

        //6.切换HEAD到目标分支
        writeContents(HEAD_FILE, "ref: refs/heads/" + branchName);

        //7.清空暂存区
        StagingArea stagingArea = readStagingArea();
        stagingArea.clear();
        saveStagingArea(stagingArea);

    }

    public static void branch(String branchName) {
        //1.初始化自检查
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        //1.检查分支是否存在
        File newBranchFile = join(HEADS_DIR, branchName);
        if (newBranchFile.exists()) {
            System.out.println("A branch with that name already exists");
            return;
        }

        //2.获取HEAD commit hash
        String headCommitHash = getHeadCommitHash();

        //3.创建新分支
        writeContents(newBranchFile, headCommitHash);
    }

    //删除分支并不是要删除分支上的所以commit，而是将分支删除而已
    public static void rmBranch(String branchName) {
        //1.初始化自检查
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        //2.检查要删除的分支是否存在
        File branchToRmFile = join(HEADS_DIR, branchName);
        if (!branchToRmFile.exists()) {
            System.out.println("A branch with that name does not exists");
            return;
        }

        //3.检查是否试图删除当前分支
        String curBranch = readContentsAsString(HEAD_FILE).replace("refs: refs/heads/", "");
        if (curBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }

        //4.删除
        if (!branchToRmFile.delete()) {
            System.out.println("Failed to delete branch.");
        }
    }

    public static void reset(String commitID) {
        //1.初始化自检查
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        //2.检查commitID是否存在
        File commitFile = join(OBJECTS_DIR, commitID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        //3.加载所需commit
        Commit targetCommit = readCommit(commitID);
        Commit headCommit = getHeadCommit();

        //4.检查文件
        List<String> cwdFiles = plainFilenamesIn(CWD);
        if (cwdFiles != null) {
            Map<String, String> targetTrackedFiles = targetCommit.getTrackedFiles();
            Map<String, String> headTrackedFiles = headCommit.getTrackedFiles();

            for (String fileName : cwdFiles) {
                boolean isHead = headTrackedFiles.containsKey(fileName);
                boolean isTarget = targetTrackedFiles.containsKey(fileName);

                if (!isHead && isTarget) {
                    System.out.println("There is a untracked file in the way; delete it, or add it and commit it firs.");
                    return;
                }
            }
        }

        //5.更新文件
        for (String fileName : targetCommit.getTrackedFiles().keySet()) {
            checkoutFileFromCommit(commitID, fileName);
        }
        //6.删除
        for (String fileName : headCommit.getTrackedFiles().keySet()) {
            if (!targetCommit.getTrackedFiles().containsKey(fileName)) {
                restrictedDelete(join(CWD, fileName));
            }
        }

        //7.移动HEAD指针
        String currentBranchName = readContentsAsString(HEAD_FILE).replace("ref: refs/heads/", "");
        File currentBranchFile = join(HEADS_DIR, currentBranchName);
        writeContents(currentBranchFile, commitID);

        //8.清空暂存区
        StagingArea stage = readStagingArea();
        stage.clear();
        saveStagingArea(stage);
    }

    //merge，最后的命令，将指定分支的历史和更改合并到当前分支
    public static void merge(String branchName) {
        //1. 前置检查
        StagingArea stagingArea = readStagingArea(); // 假设 readStagingArea() 能读取暂存区对象
        if (!stagingArea.getFieldToAdd().isEmpty() || !stagingArea.getFieldToRemove().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return; // 使用 return 代替 System.exit(0) 更优雅
        }

        File givenBranchFile = join(HEADS_DIR, branchName);
        if (!givenBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        String currentBranchName = readContentsAsString(HEAD_FILE).replace("ref: refs/heads/", ""); // 假设这个辅助方法能正确读取当前分支名
        if (currentBranchName.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        // 核心ID准备
        String currentHeadId = getHeadCommitHash(); // 获取当前分支的头提交ID
        String givenHeadId = readContentsAsString(givenBranchFile); // 获取目标分支的头提交ID
        Commit currentCommit = readCommit(currentHeadId);
        Commit givenCommit = readCommit(givenHeadId);

        //5. 寻找分叉点
        // 这个算法更健壮且正确
        Set<String> currentAncestors = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(currentHeadId);

        // 通过广度优先搜索找到当前分支所有祖先
        while (!queue.isEmpty()) {
            String commitId = queue.poll();
            if (commitId == null) {
                continue;
            }
            currentAncestors.add(commitId);
            Commit commit = readCommit(commitId);
            for (String parentId : commit.getParents()) {
                queue.add(parentId);
            }
        }

        String splitPointId = null;
        queue.add(givenHeadId);

        // 在目标分支的祖先中，找到第一个也存在于当前分支祖先集合里的提交
        while (!queue.isEmpty()) {
            String commitId = queue.poll();
            if (commitId == null) continue;

            if (currentAncestors.contains(commitId)) {
                splitPointId = commitId;
                break; // 找到了最近的公共祖先
            }
            Commit commit = readCommit(commitId);
            for (String parentId : commit.getParents()) {
                queue.add(parentId);
            }
        }

        //6. 处理特殊合并情况
        if (splitPointId.equals(givenHeadId)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitPointId.equals(currentHeadId)) {
            System.out.println("Current branch fast-forwarded.");
            // 快速前进：只需将当前分支指针指向目标分支，并检出
            checkoutBranch(branchName); // checkoutBranch 会更新工作区和 HEAD 文件
            return;
        }

        // 4. 重新检查未跟踪文件 (正确的位置和逻辑)
        // 必须在知道合并细节后，执行操作前检查
        Map<String, String> splitFiles = readCommit(splitPointId).getTrackedFiles();
        Map<String, String> currentFiles = currentCommit.getTrackedFiles();
        Map<String, String> givenFiles = givenCommit.getTrackedFiles();
        Set<String> allFileNames = new HashSet<>();
        allFileNames.addAll(splitFiles.keySet());
        allFileNames.addAll(currentFiles.keySet());
        allFileNames.addAll(givenFiles.keySet());

        List<String> cwdFiles = plainFilenamesIn(CWD);
        for (String fileName : allFileNames) {
            String givenHash = givenFiles.get(fileName);
            String currentHash = currentFiles.get(fileName);
            // 如果一个文件在当前分支未被跟踪，但合并操作需要创建/修改它，则报错
            if (!currentFiles.containsKey(fileName) && givenHash != null && cwdFiles.contains(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }


        // 7. 三方合并 (补全版) ---
        boolean conflictOccurred = false;
        for (String fileName : allFileNames) {
            String splitHash = splitFiles.get(fileName);
            String currentHash = currentFiles.get(fileName);
            String givenHash = givenFiles.get(fileName);

            boolean modifiedInCurrent = !Objects.equals(splitHash, currentHash);
            boolean modifiedInGiven = !Objects.equals(splitHash, givenHash);

            if (modifiedInCurrent && modifiedInGiven) {
                // 情况A: 两个分支都修改了
                if (!Objects.equals(currentHash, givenHash)) {
                    conflictOccurred = true;
                    // --- 冲突处理逻辑 ---
                    // 读取当前分支文件内容
                    String currentContent = (currentHash == null) ? "" : readContentsAsString(join(OBJECTS_DIR, currentHash));
                    // 读取目标分支文件内容
                    String givenContent = (givenHash == null) ? "" : readContentsAsString(join(OBJECTS_DIR, givenHash));
                    // 构建冲突文件内容并写入
                    String conflictContent = "<<<<<<< HEAD\n"
                            + currentContent
                            + "=======\n"
                            + givenContent
                            + ">>>>>>>\n";
                    File conflictFile = join(CWD, fileName);
                    writeContents(conflictFile, conflictContent);
                    // 将冲突文件暂存
                    add(fileName); // 调用你已实现的add命令的逻辑
                }
                // 如果内容相同，则无需操作 (规则 3, 4)

            } else if (!modifiedInCurrent && modifiedInGiven) {
                // 情况B: 仅在目标分支(given)中修改
                if (givenHash == null) {
                    // **规则5**: 在given中被删除 -> 调用rm的逻辑
                    rm(fileName);
                } else {
                    // **规则1 & 7**: 在given中被修改或新增 -> checkout并add
                    checkoutFileFromCommit(givenHeadId, fileName); // 从目标提交检出文件
                    add(fileName); // 暂存这个检出的文件
                }
            }
            // 情况C: 仅在当前分支中修改 (!modifiedInGiven && modifiedInCurrent)
            // 对应规则 2, 6, 8，这些都无需任何操作，因为当前分支的状态就是我们想要的。
        }

        // --- 8. 提交或报告冲突 ---
        if (conflictOccurred) {
            System.out.println("Encountered a merge conflict.");
        } else {
            // 没有冲突，创建合并提交
            String message = "Merged " + branchName + " into " + currentBranchName + ".";
            // 创建一个特殊的commit，它有两个父节点
            List<String> parents = new ArrayList<>();
            parents.add(currentHeadId); // 第一个父节点是当前分支的HEAD
            parents.add(givenHeadId);   // 第二个父节点是目标分支的HEAD

            StagingArea finalStagingArea = readStagingArea();
            Map<String, String> newTrackedFiles = new HashMap<>(currentCommit.getTrackedFiles());

            // 应用暂存区（在merge过程中被修改）的变化
            for (Map.Entry<String, String> entry : finalStagingArea.getFieldToAdd().entrySet()) {
                newTrackedFiles.put(entry.getKey(), entry.getValue());
            }
            for (String fileToRemove : finalStagingArea.getFieldToRemove().keySet()) {
                newTrackedFiles.remove(fileToRemove);
            }

            // 用这个完整的文件列表创建Commit
            Commit newMergeCommit = new Commit(message, parents, newTrackedFiles, new Date());

            // 保存commit
            saveCommit(newMergeCommit); // 复用你的 saveCommit 辅助函数
            String newCommitId = sha1(serialize(newMergeCommit));

            // 更新当前分支的HEAD指向新的合并提交
            File currentBranchFile = join(HEADS_DIR, currentBranchName);
            writeContents(currentBranchFile, newCommitId);

            // 清空暂存区
            finalStagingArea.clear();
            saveStagingArea(finalStagingArea);
        }
    }

    //辅助函数
    //从文件中读取StagingArea对象
    private static StagingArea readStagingArea() {
        return readObject(STAGING_FILE, StagingArea.class);
    }

    //保存StagingArea对象，将暂存区对象写入
    private static void saveStagingArea(StagingArea SA) {
        writeObject(STAGING_FILE, SA);
    }

    //从文件中读取Commit对象，根据哈希值
    private static Commit readCommit(String commitHash) {
        if (commitHash == null) {
            return null;
        }
        File commitFile = join(OBJECTS_DIR, commitHash);
        if (!commitFile.exists()) {
            throw new GitletException("Commit with hash" + commitHash + " does not exist.");
        }
        return readObject(commitFile, Commit.class);
    }

    //获取当前HEAD指向的Commit的hash值
    private static String getHeadCommitHash() {
        String headFile = readContentsAsString(HEAD_FILE).replace("ref: ", "");
        File branchFile = join(GITLET_DIR, headFile);
        return readContentsAsString(branchFile);
    }

    //获取当前HEAD指向的Commit对象
    private static Commit getHeadCommit() {
        String headHash = getHeadCommitHash();
        return readCommit(headHash);
    }

    //保存Commit对象到Objects目录
    private static void saveCommit(Commit commit) {
        byte[] commitBytes = serialize(commit);
        String commitHash = sha1(commitBytes);
        File commitFile = join(OBJECTS_DIR, commitHash);
        writeObject(commitFile, commit);
    }

    //更新分支最新commit，并不需要动HEAD，HEAD指向的分支目前还是最新的
    private static void updateCurBranchHead(String newCommitHash) {
        String refPath = readContentsAsString(HEAD_FILE).replace("ref: ", "");
        File branchPath = join(GITLET_DIR, refPath);
        writeContents(branchPath, newCommitHash);
    }
}
