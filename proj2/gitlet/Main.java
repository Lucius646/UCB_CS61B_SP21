package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 * @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }

        String firstArg = args[0];
        try { // <--- try 从这里开始
            switch(firstArg) {
                case "init":
                    if (args.length != 1) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.init();
                    break;
                case "add":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.add(args[1]);
                    break;
                case "commit":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.commit(args[1]);
                    break;
                case "rm":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.rm(args[1]);
                    break;
                case "log":
                    if (args.length != 1) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.log();
                    break;
                case "global-log":
                    if (args.length != 1) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.globalLog();
                    break;
                case "find":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.find(args[1]);
                    break;
                case "status":
                    if (args.length != 1) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.status();
                    break;
                case "checkout":
                    if (args.length == 2) {
                        String branchName = args[1];
                        Repository.checkoutBranch(branchName);
                    } else if (args.length == 3 && args[1].equals("--")) {
                        String fileName = args[2];
                        Repository.checkoutFileFromHead(fileName);
                    } else if (args.length == 4 && args[2].equals("--")) {
                        String commitID = args[1];
                        String fileName = args[3];
                        Repository.checkoutFileFromCommit(commitID, fileName);
                    } else {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    break;
                case "branch":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.branch(args[1]);
                    break;
                case "rm-branch":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.rmBranch(args[1]);
                    break;
                case "reset":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.reset(args[1]);
                    break;
                case "merge":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands");
                        System.exit(0);
                    }
                    Repository.merge(args[1]);
                    break;
                default:
                    System.out.println("No command with that name exists.");
                    System.exit(0);
            }
        } catch (GitletException e) { // <--- catch 紧跟在 try 的 '}' 之后
            // 从异常对象中获取消息，并打印出来
            System.out.println(e.getMessage());
            // 正常退出程序，退出码为 0
            System.exit(0);
        }
    }
}