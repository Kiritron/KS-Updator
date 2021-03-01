/*
 * Copyright 2021 Kiritron's Space
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.kiritron.updator;

import space.kiritron.duke_cli.httpconn;
import space.kiritron.pixel.CheckerDIR;
import space.kiritron.pixel.GDate;
import space.kiritron.pixel.filefunc.FileControls;
import space.kiritron.pixel.filefunc.GetPathOfAPP;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static space.kiritron.pixel.filefunc.DirControls.SearchDir;

/**
 * @author Киритрон Стэйблкор
 */

public class main {
    private static frame fr = new frame();
    private static Image img;

    public static void main(String[] args) {
        if (args.length != 0) { // Проверяется, не запущено ли приложение случайным образом. Если вводных параметров нет, то программа закрывается и ничего не происходит.
            String codename_app = args[0];
            codename_app = codename_app.replace("--", "");

            // Подобный код есть и в init классе КС приложений, но конкретно здесь он выполняет функции проверки, а есть ли вообще версии(а соответственно и обновления)
            // для данного приложения. Таким образом, исключаются ненужные ковыряния на веб-сервере.
            String checkVersionOut = httpconn.checkVersion("https://cdn.kiritron.space/app-versions/" + codename_app,false, false, "null");
            if (checkVersionOut.contains("DIFFERENCE_FINDED")) {
                // Тут уже Апдэйтору очевидно ясно, что у целевого приложения есть версии, а значит есть и обновления, следовательно, можно загружать и устанавливать.
                CheckerDIR.Check("cache");
                CheckerDIR.Check("cache" + GetPathOfAPP.GetSep() + "downloaded_update");

                img = new ImageIcon(main.class.getResource("logo.png")).getImage();

                fr.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                fr.setIconImage(img);
                fr.setVisible(true);
                downloadApp(codename_app);
            } else {
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    private static void downloadApp(String codename_of_app_on_ks_cdn) {
        new Thread(() -> {
            try {
                URL url = new URL("https://cdn.kiritron.space/downloads/" + codename_of_app_on_ks_cdn + "/" + codename_of_app_on_ks_cdn + "-latest.zip");
                HttpsURLConnection httpConnection = (HttpsURLConnection) (url.openConnection());

                long fileSize = httpConnection.getContentLength();
                long chunkSize = fileSize / 100;
                fr.textarea.append(" [" + GDate.GetCurDateAndTime + "] Начинаю скачивание обновления по адресу https://cdn.kiritron.space/downloads/" + codename_of_app_on_ks_cdn + "/" + codename_of_app_on_ks_cdn + "-latest.zip...\n");
                try (BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(GetPathOfAPP.GetPathWithSep() + "cache" + GetPathOfAPP.GetSep() + "downloaded_update" + GetPathOfAPP.GetSep() + codename_of_app_on_ks_cdn + ".zip"), 1024)) {

                    byte[] data = new byte[1024];
                    long downloaded = 0;
                    int read = 0;

                    while ((read = in.read(data, 0, 1024)) != -1) {
                        out.write(data, 0, read);

                        downloaded += read;

                        final int progress = (int) (downloaded / chunkSize);
                        fr.textarea.append(" [" + GDate.GetCurDateAndTime + "] Скачивание " + codename_of_app_on_ks_cdn + ". Прогресс " + progress + "%.\n");
                    }
                }
            } catch (FileNotFoundException exc) {
                fr.textarea.append( " [" + GDate.GetCurDateAndTime + "] Не могу скачать обновление. Файла нет на сервере.\n" );
                return;
            } catch (IOException exc) {
                fr.textarea.append( " [" + GDate.GetCurDateAndTime + "] Не могу скачать обновление. Ошибка ввода/вывода.\n" );
                return;
            }

            SwingUtilities.invokeLater(() -> {
                fr.textarea.append( " [" + GDate.GetCurDateAndTime + "] Обновление скачано. Устанавливаю...\n" );
                installApp(codename_of_app_on_ks_cdn);
            });

        }).start();
    }

    private static void installApp(String codename_of_app) {
        // Киритрон: Данный метод основан на примере от baeldung.

        if (SearchDir(GetPathOfAPP.GetPathWithSep() + "cache")) {
            if (SearchDir(GetPathOfAPP.GetPathWithSep() + "cache" + GetPathOfAPP.GetSep() + "downloaded_update")) {
                String fileZip = GetPathOfAPP.GetPathWithSep() + "cache" + GetPathOfAPP.GetSep() + "downloaded_update" + GetPathOfAPP.GetSep() + codename_of_app + ".zip";
                File destDir = new File(GetPathOfAPP.GetPathWithSep());
                byte[] buffer = new byte[1024];
                try {
                    fr.textarea.append( " [" + GDate.GetCurDateAndTime + "] Установка начата. Пожалуйста, подождите...\n" );
                    ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
                    ZipEntry zipEntry = zis.getNextEntry();
                    while (zipEntry != null) {
                        while (zipEntry != null) {
                            File newFile = newFile(destDir, zipEntry);
                            if (zipEntry.isDirectory()) {
                                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                                    throw new IOException("Failed to create directory " + newFile);
                                }
                            } else {
                                // fix for Windows-created archives
                                File parent = newFile.getParentFile();
                                if (!parent.isDirectory() && !parent.mkdirs()) {
                                    throw new IOException("Failed to create directory " + parent);
                                }
                                // write file content
                                FileOutputStream fos = new FileOutputStream(newFile);
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, len);
                                }
                                fos.close();
                            }
                            zipEntry = zis.getNextEntry();
                        }
                    }
                    zis.closeEntry();
                    zis.close();

                    fr.textarea.append( " [" + GDate.GetCurDateAndTime + "] Обновление установлено! Можете закрыть данное приложение и открыть обновлённое приложение.\n" );

                    FileControls.DeleteFile(GetPathOfAPP.GetPathWithSep() + "cache" + GetPathOfAPP.GetSep() + "downloaded_update" + GetPathOfAPP.GetSep() + codename_of_app + ".zip");
                } catch (FileNotFoundException e) {
                    fr.textarea.append( " [" + GDate.GetCurDateAndTime + "] Обновление не установлено. Не могу найти архив с обновлением, для последующей распаковки.\n" );
                } catch (IOException e) {
                    fr.textarea.append( " [" + GDate.GetCurDateAndTime + "] Обновление не установлено. Ошибка ввода/вывода. Возможно, нет прав на установку обновления.\n" );
                }

            }
        } else {
            fr.textarea.append( " [" + GDate.GetCurDateAndTime + "] Обновление не установлено. Нету папки, куда должен был быть скачан архив с обновлением.\n" );
        }
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        // Киритрон: И этот метод тоже основан на примере от baeldung.

        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
