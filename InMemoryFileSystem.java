package ass.com;


	import java.util.HashMap;
	import java.util.Map;
	import java.util.Scanner;

	class File {
	    private String content;

	    public File(String content) {
	        this.content = content;
	    }

	    public String getContent() {
	        return content;
	    }

	    public void setContent(String content) {
	        this.content = content;
	    }
	}

	class Directory {
	    private Map<String, Directory> directories;
	    private Map<String, File> files;

	    public Directory() {
	        this.directories = new HashMap<>();
	        this.files = new HashMap<>();
	    }

	    public Map<String, Directory> getDirectories() {
	        return directories;
	    }

	    public Map<String, File> getFiles() {
	        return files;
	    }
	}

	public class InMemoryFileSystem {
	    private Directory root;
	    private Directory currentDirectory;

	    public InMemoryFileSystem() {
	        this.root = new Directory();
	        this.currentDirectory = root;
	    }

	    public void mkdir(String dirname) {
	        currentDirectory.getDirectories().put(dirname, new Directory());
	    }

	    public void cd(String path) {
	        if (path.equals("/")) {
	            currentDirectory = root;
	        } else if (path.startsWith("/")) {
	            currentDirectory = getDirectoryByPath(path, root);
	        } else {
	            currentDirectory = getDirectoryByPath(path, currentDirectory);
	        }
	    }

	    public void ls(String path) {
	        if (path.equals(".")) {
	            listContents(currentDirectory);
	        } else {
	            Directory targetDirectory = getDirectoryByPath(path, currentDirectory);
	            listContents(targetDirectory);
	        }
	    }

	    public void touch(String filename) {
	        currentDirectory.getFiles().put(filename, new File(""));
	    }

	    public void cat(String filename) {
	        File file = currentDirectory.getFiles().get(filename);
	        if (file != null) {
	            System.out.println(file.getContent());
	        } else {
	            System.out.println("File not found.");
	        }
	    }

	    public void echo(String text, String filename) {
	        File file = currentDirectory.getFiles().get(filename);
	        if (file != null) {
	            file.setContent(text);
	        } else {
	            System.out.println("File not found.");
	        }
	    }

	    public void mv(String source, String destination) {
	        Directory sourceParent = getParentDirectoryByPath(source);
	        String itemName = getItemNameByPath(source);
	        Directory destinationDirectory = getDirectoryByPath(destination, root);

	        if (sourceParent != null && destinationDirectory != null) {
	            if (sourceParent.getDirectories().containsKey(itemName)) {
	                destinationDirectory.getDirectories().put(itemName, sourceParent.getDirectories().remove(itemName));
	            } else if (sourceParent.getFiles().containsKey(itemName)) {
	                destinationDirectory.getFiles().put(itemName, sourceParent.getFiles().remove(itemName));
	            } else {
	                System.out.println("Item not found.");
	            }
	        } else {
	            System.out.println("Invalid source or destination.");
	        }
	    }

	    public void cp(String source, String destination) {
	        Directory sourceParent = getParentDirectoryByPath(source);
	        String itemName = getItemNameByPath(source);
	        Directory destinationDirectory = getDirectoryByPath(destination, root);

	        if (sourceParent != null && destinationDirectory != null) {
	            if (sourceParent.getDirectories().containsKey(itemName)) {
	                destinationDirectory.getDirectories().put(itemName, new Directory());
	            } else if (sourceParent.getFiles().containsKey(itemName)) {
	                destinationDirectory.getFiles().put(itemName, new File(sourceParent.getFiles().get(itemName).getContent()));
	            } else {
	                System.out.println("Item not found.");
	            }
	        } else {
	            System.out.println("Invalid source or destination.");
	        }
	    }

	    public void rm(String path) {
	        Directory parentDirectory = getParentDirectoryByPath(path);
	        String itemName = getItemNameByPath(path);

	        if (parentDirectory != null) {
	            if (parentDirectory.getDirectories().containsKey(itemName)) {
	                parentDirectory.getDirectories().remove(itemName);
	            } else if (parentDirectory.getFiles().containsKey(itemName)) {
	                parentDirectory.getFiles().remove(itemName);
	            } else {
	                System.out.println("Item not found.");
	            }
	        } else {
	            System.out.println("Invalid path.");
	        }
	    }

	    private Directory getDirectoryByPath(String path, Directory startDirectory) {
	        String[] components = path.split("/");
	        Directory currentDirectory = startDirectory;

	        for (String component : components) {
	            if (component.equals("..")) {
	                currentDirectory = getParentDirectory(currentDirectory);
	            } else {
	                currentDirectory = currentDirectory.getDirectories().get(component);
	            }

	            if (currentDirectory == null) {
	                break;
	            }
	        }

	        return currentDirectory;
	    }

	    private void listContents(Directory directory) {
	        System.out.println("Directories:");
	        for (String dirName : directory.getDirectories().keySet()) {
	            System.out.println(dirName);
	        }

	        System.out.println("\nFiles:");
	        for (String fileName : directory.getFiles().keySet()) {
	            System.out.println(fileName);
	        }
	    }

	    private Directory getParentDirectoryByPath(String path) {
	        String[] components = path.split("/");
	        if (components.length > 1) {
	            String parentPath = String.join("/", components);
	            return getDirectoryByPath(parentPath, root);
	        }
	        return null;
	    }

	    private Directory getParentDirectory(Directory directory) {
	        // This assumes the root directory has no parent
	        if (directory == root) {
	            return root;
	        }

	        for (Directory parent : root.getDirectories().values()) {
	            if (parent.getDirectories().containsValue(directory) || parent.getFiles().containsValue(directory)) {
	                return parent;
	            }
	        }

	        return null;
	    }

	    private String getItemNameByPath(String path) {
	        String[] components = path.split("/");
	        return components[components.length - 1];
	    }

	    public static void main(String[] args) {
	        InMemoryFileSystem fileSystem = new InMemoryFileSystem();
	        Scanner scanner = new Scanner(System.in);

	        while (true) {
	            System.out.print("> ");
	            String command = scanner.nextLine().trim();

	            if (command.equalsIgnoreCase("exit")) {
	                break;
	            }

	            try {
	                String[] tokens = command.split("\\s+");
	                String operation = tokens[0];

	                switch (operation.toLowerCase()) {
	                    case "mkdir":
	                        fileSystem.mkdir(tokens[1]);
	                        break;
	                    case "cd":
	                        fileSystem.cd(tokens[1]);
	                        break;
	                    case "ls":
	                        if (tokens.length > 1) {
	                            fileSystem.ls(tokens[1]);
	                        } else {
	                            fileSystem.ls(".");
	                        }
	                        break;
	                    case "touch":
	                        fileSystem.touch(tokens[1]);
	                        break;
	                    case "cat":
	                        fileSystem.cat(tokens[1]);
	                        break;
	                    case "echo":
	                        fileSystem.echo(tokens[1], tokens[3]);
	                        break;
	                    case "mv":
	                        fileSystem.mv(tokens[1], tokens[2]);
	                        break;
	                    case "cp":
	                        fileSystem.cp(tokens[1], tokens[2]);
	                        break;
	                    case "rm":
	                        fileSystem.rm(tokens[1]);
	                        break;
	                    default:
	                        System.out.println("Invalid command.");
	                }
	            } catch (Exception e) {
	                System.out.println("Error: " + e.getMessage());
	            }
	        }

	        scanner.close();
	    }
	}



