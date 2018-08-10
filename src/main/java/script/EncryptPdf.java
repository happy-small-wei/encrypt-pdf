package script;

import com.itextpdf.kernel.crypto.BadPasswordException;
import com.itextpdf.kernel.pdf.*;

import java.io.File;

public class EncryptPdf {
    private String sourceDirPath, targetDirPath, userPassword, ownerPassword;

    private EncryptPdf(String sourceDirPath, String targetDirPath, String userPassword, String ownerPassword) {
        this.sourceDirPath = sourceDirPath;
        this.targetDirPath = targetDirPath;
        this.userPassword = userPassword;
        this.ownerPassword = ownerPassword;
    }

    private void tranverseDirectory() {
        File sourceFile = new File(sourceDirPath);
        if(sourceFile.isDirectory()) {
            tranverseDirectory(sourceDirPath, targetDirPath);
        } else if(isPdfExtension(sourceFile)) {
            generateEncryptedPdf(sourceFile, targetDirPath);
        } else {
            System.out.println("Not a directory nor a pdf file!");
        }
    }

    private void tranverseDirectory(String sourcePath, String targetDirPath) {
        File targetFile = new File(targetDirPath);
        if(!targetFile.exists() && !targetFile.mkdir()) {
            System.out.println("Directory does not exist and cannot be created: " + targetDirPath);
            return;
        }
        File sourceFile = new File(sourcePath);
        if(sourceFile.exists()) {
            File[] files = sourceFile.listFiles();
            if(files != null) {
                for (File f: files) {
                    String newTargetPath = targetDirPath + "/" + f.getName();
                    if(f.isDirectory()){
                        tranverseDirectory(f.getAbsolutePath(), newTargetPath);
                    } else if(isPdfExtension(f)) {
                        try {
                            generateEncryptedPdf(f, generateEncryptedFileName(newTargetPath));
                        }catch (Exception e) {
                            System.out.println("[ERROR]: " + f.getAbsolutePath());
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            System.out.println("Source directory does not exist!");
        }
    }

    private void generateEncryptedPdf(File file, String targetFilePath) {

        WriterProperties prop = new WriterProperties();
        prop.setStandardEncryption(
                userPassword.getBytes(), ownerPassword.getBytes(), 0, EncryptionConstants.ENCRYPTION_AES_256);
        try {
            PdfReader reader = null;
            PdfWriter writer = null;
            PdfDocument pdfDoc = null;
            boolean needDelete = false;
            try{
                reader = new PdfReader(file.getAbsoluteFile());
                writer = new PdfWriter(targetFilePath, prop);
                pdfDoc = new PdfDocument(reader.setUnethicalReading(true), writer);
            } catch (BadPasswordException e) {
                System.out.println("Encrypted pdf: " + file.getAbsolutePath());
                needDelete = writer != null;
            } catch (Exception e) {
                e.printStackTrace();
                needDelete = writer != null;
            } finally {
                if(reader != null && reader.isCloseStream()) {
                    reader.close();
                }
                if(needDelete) {
                    writer.close();
                    new File(targetFilePath).delete();
                }
                if(pdfDoc != null && !pdfDoc.isClosed()) {
                    pdfDoc.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isPdfExtension(File file) {
        String fileName = file.getName();
        int beginPosOfExtension = fileName.lastIndexOf('.');
        return beginPosOfExtension != -1 && fileName.substring(beginPosOfExtension).equals(".pdf");
    }

    private String generateEncryptedFileName(String path) {
        int index = path.lastIndexOf(".");
        String front = path.substring(0, index);
        String back = path.substring(index);
        return front + "_enc" + back;
    }

    public static void main(String[] args) {
        System.out.println(((-4) << 1));
        if(args.length != 4) {
            System.out.println(
                    "Exact 4 arguments need to be given: " + "sourceDirPath, targetDirPath, userPassword, ownerPassword");
            return;
        }
        EncryptPdf generatePdf = new EncryptPdf(args[0], args[1], args[2], args[3]);
        generatePdf.tranverseDirectory();
    }
}
