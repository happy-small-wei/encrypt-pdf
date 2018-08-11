import com.itextpdf.kernel.crypto.BadPasswordException;
import com.itextpdf.kernel.pdf.*;

import java.io.File;

public class EncryptPdf {
    private String sourcePath, targetDirPath, userPassword, ownerPassword;

    private EncryptPdf(String sourcePath, String targetDirPath, String userPassword, String ownerPassword) {
        this.sourcePath = sourcePath;
        this.targetDirPath = targetDirPath;
        this.userPassword = userPassword;
        this.ownerPassword = ownerPassword;
    }

    private void encrypt() {
        File sourceFile = new File(sourcePath);
        if(!sourceFile.isDirectory() && isPdfExtension(sourceFile)) {
            System.out.println("SourcePath is not a directory nor a pdf file!");
            return;
        }
        File targetDirectory = new File(targetDirPath);
        if(!(targetDirectory.exists() && targetDirectory.isDirectory()) && !targetDirectory.mkdir()) {
            System.out.println("Directory does not exist and cannot be created: " + targetDirPath);
            return;
        }
        encrypt(sourceFile, targetDirPath);
    }

    private void encrypt(File sourceFile, String targetDirPath) {
        if(sourceFile.isDirectory()) {
            File[] files = sourceFile.listFiles();
            if(files != null) {
                for (File f: files) {
                    encrypt(f, targetDirPath);
                }
            } else {
                System.out.println("[ERROR]: cannot get file list of directory " + sourceFile.getAbsoluteFile());
            }
        } else if(isPdfExtension(sourceFile)){
            String targetFilePath = targetDirPath + File.separator + generateEncryptedFileName(sourceFile.getName());
            generateEncryptedPdf(sourceFile, targetFilePath);
        }
    }

    private void generateEncryptedPdf(File sourceFile, String targetFilePath) {
        WriterProperties prop = new WriterProperties();
        prop.setStandardEncryption(
                userPassword.getBytes(), ownerPassword.getBytes(),
                0, EncryptionConstants.ENCRYPTION_AES_256);
        try {
            PdfReader reader = null;
            PdfWriter writer = null;
            PdfDocument pdfDoc = null;
            boolean needDelete = false;
            try{
                reader = new PdfReader(sourceFile);
                writer = new PdfWriter(targetFilePath, prop);
                pdfDoc = new PdfDocument(reader.setUnethicalReading(true), writer);
            } catch (BadPasswordException e) {
                System.out.println("Encrypted pdf: " + sourceFile.getAbsolutePath());
                needDelete = writer != null;
            } catch (Exception e) {
                System.out.println("[ERROR]: " + sourceFile.getAbsolutePath());
                e.printStackTrace();
                needDelete = writer != null;
            } finally {
                if(needDelete) {
                    writer.close();
                    new File(targetFilePath).delete();
                }
                if(pdfDoc != null && !pdfDoc.isClosed()) {
                    pdfDoc.close();
                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR]: " + sourceFile.getAbsolutePath());
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
                    "Exact 4 arguments need to be given: " + "sourcePath, targetDirPath, userPassword, ownerPassword");
            return;
        }
        EncryptPdf generatePdf = new EncryptPdf(args[0], args[1], args[2], args[3]);
        generatePdf.encrypt();
    }
}
