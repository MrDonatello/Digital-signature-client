package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;


import javax.crypto.Cipher;
import java.io.*;
import java.security.*;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Controller {

    private final String locationPublicKey = "public.key";
    private final String locationPrivateKey = "private.key";
    private String signFile = "data.sign";
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @FXML
    private Button sign;

    @FXML
    private TextArea text;

    @FXML
    void initialize() {
        checkKey();
        sign.setOnAction(event -> {
            checkKey();
            try {
                // Создание подписи (хэш-функции с RSA)
                Signature signature = Signature.getInstance("SHA256withRSA");
                // Инициализация подписи закрытым ключом
                signature.initSign(privateKey);
                // Формирование цифровой подписи сообщения  закрытым ключом
                signature.update(text.getText().getBytes());
                // Байтовый массив цифровой подписи
                byte[] realSignature = signature.sign();
                // Base64.getEncoder().encodeToString(realSignature);

                // Сохранение цифровой подписи сообщения в файл
                FileOutputStream fos = new FileOutputStream(signFile);
                fos.write(realSignature);
                fos.close();
            } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    void open() {
        FileChooser fileChooser = new FileChooser();
        File fileSelected = fileChooser.showOpenDialog(null);
        signFile = fileSelected.getName().substring(0, fileSelected.getName().lastIndexOf('.')) + ".sign";
        StringBuilder text1 = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileSelected), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                text1.append(line);
            }
            br.close();
            text.setText(text1.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void save() {
        FileChooser fileChooser = new FileChooser();
        File fileSave = fileChooser.showSaveDialog(null);
        if (fileSave != null) {
            try {
                Writer out = new OutputStreamWriter(new FileOutputStream(fileSave), "UTF-8");
                out.write(text.getText());
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void alertKey() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning");
        alert.setHeaderText("Keys does not exist");
        alert.setContentText("Do you want generate keys?");
        if (alert.showAndWait().orElse(null) == ButtonType.OK) {
            createKey();
            try {
                saveKey(locationPublicKey, publicKey);
                saveKey(locationPrivateKey, privateKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Object readKey(String filePath) {
        Object object = null;
        try {
            FileInputStream fis = new FileInputStream(filePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            object = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }

    private void saveKey(final String filePath, final Object key)
            throws IOException {
        if (key != null) {
            FileOutputStream fos = new FileOutputStream(filePath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(key);
            oos.close();
            fos.close();
        }
    }

    private void createKey() {
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");// создание и назначение алгоритма RSA
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyPairGen.initialize(1024); // инициализируем keyPairGen для генерации ключей размером 1024 бита
        KeyPair keyPair = keyPairGen.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
    }

    private void checkKey() {
        publicKey = (PublicKey) readKey(locationPublicKey);
        privateKey = (PrivateKey) readKey(locationPrivateKey);
        if (publicKey == null || privateKey == null) {
            alertKey();
        }
    }

    public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] cipherText = encryptCipher.doFinal(plainText.getBytes(UTF_8));

        return Base64.getEncoder().encodeToString(cipherText);
    }
}

