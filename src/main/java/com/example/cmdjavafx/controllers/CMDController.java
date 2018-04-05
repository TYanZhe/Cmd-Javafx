package com.example.cmdjavafx.controllers;

import com.example.cmdjavafx.CmdjavafxApplication;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class CMDController {
    public TextArea textArea;

    private String currPath;
    private String text;

    private boolean lastCommandIsEnter = false;
    private boolean cls = false;

    //команда - описание
    private TreeMap<String, String> commands = new TreeMap<>();
    private TreeMap<String, String> params = new TreeMap<>();
    private List<String> listLastComm = new ArrayList<>();
    private static int indexLastComm = 0;

    @FXML
    protected void initialize() throws MalformedURLException {
        currPath = System.getProperty("user.dir");

        CmdjavafxApplication.getInstance().updateTitle(currPath);

        textArea.setText(textArea.getText() + "\n" + currPath + ">");
        textArea.positionCaret(textArea.getText().length());


        text = textArea.getText();

        commands.put("copy", "Копирование одного или нескольких файлов в другое место. --> copy FILE NEWFILE");
        commands.put("cd", "Вывод имени либо смена текущей папки.");
        commands.put("cls", "Очистка экрана.");
        commands.put("del", "Удаление одного или нескольких файлов.");
        commands.put("dir", "Вывод списка файлов и подпапок из указанной папки");
        commands.put("exit", "Завершает работу программы CMD.EXE (интерпретатора командных строк).");
        commands.put("help", "Выводит справочную информацию о командах Windows.");

        params.put("-a", "Применить для всех элементов заданного типа, тип указывается после параметра - .txt");
        params.put("/d", "Параметр /D используется для одновременной смены текущего диска и каталога. CD [/D] [диск:][путь]");
    }

    public void execute(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            lastCommandIsEnter = true;

            String line = textArea.getText().substring(text.length()).toLowerCase();
            listLastComm.add(line);
            indexLastComm++;
            String[] strings = line.split(" ");
            String command = strings[0];

            switch (command) {
                case "copy": {
                    if (strings.length == 3) {
                        if (!strings[1].equals("nul") && strings[2] != null) {
                            File folder = new File(currPath);
                            File[] listOfFiles = folder.listFiles();

                            if (listOfFiles != null) {
                                boolean f = Arrays.stream(listOfFiles).filter(File::isFile).anyMatch(file -> file.getName().equals(strings[1]));
                                if (!f) {
                                    textArea.setText(textArea.getText() + "\n" + "Не удается найти указанный файл.");
                                    return;
                                }
                            } else {
                                textArea.setText(textArea.getText() + "\n" + "Не удается найти указанный файл.");
                                return;
                            }

                            File oldFile = new File(strings[1]);
                            File newFile = new File(strings[2]);
                            BufferedReader reader = new BufferedReader(new FileReader(oldFile));
                            FileWriter fileWriter = new FileWriter(newFile);

                            reader.lines().forEach(s -> {
                                try {
                                    fileWriter.write(s);
                                    fileWriter.write("\n");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });

                            reader.close();
                            fileWriter.close();

                            textArea.setText(textArea.getText() + "\n" + "Скопировано файлов:" + String.format("%10s%20s", " ", "1"));
                        } else {
                            if (strings[2] != null) {
                                File newFile = new File(strings[2]);
                                FileWriter fileWriter = new FileWriter(newFile);
                                fileWriter.close();
                            }
                        }
                    } else {
                        textArea.setText(textArea.getText() + "\n\"" + line + "\" не является внутренней или внешней\n" +
                                " командой, исполняемой программой или пакетным файлом.");
                    }

                    break;
                }
                case "cd": {
                    if (strings.length > 1) {
                        if (strings[1].equals("/d")) {
                            if (strings.length == 3) {
                                String[] path = strings[2].replace("\\", "/").split("/");
                                File folder = new File(path[0]);
                                File[] listOfFiles = folder.listFiles();
                                currPath = path[0].toUpperCase();
                                if (listOfFiles != null) {
                                    for (int i = 1; i < path.length; i++) {
                                        int finalI = i;
                                        boolean f = Arrays.stream(listOfFiles).filter(File::isDirectory).anyMatch(file -> file.getName().equals(path[finalI]));
                                        if (f) {
                                            currPath = currPath + "\\" + path[finalI];
                                        } else {
                                            textArea.setText(textArea.getText() + "\nСистеме не удается найти указанный путь.");
                                        }
                                    }
                                }
                            } else {
                                textArea.setText(textArea.getText() + "\n\"" + line + "\" не является внутренней или внешней\n" +
                                        " командой, исполняемой программой или пакетным файлом.");
                            }
                        } else {
                            String[] path = strings[1].replace("\\", "/").split("/");
                            File folder = new File(currPath);
                            File[] listOfFiles = folder.listFiles();
                            if (listOfFiles != null) {
                                for (String p : path) {
                                    if (p.equals("..")) {
                                        currPath = folder.getParent();
                                    } else {
                                        boolean f = Arrays.stream(listOfFiles).filter(File::isDirectory).anyMatch(file -> file.getName().equals(p));
                                        if (f) {
                                            currPath = currPath + "\\" + p;
                                        } else {
                                            textArea.setText(textArea.getText() + "\nСистеме не удается найти указанный путь.");
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        textArea.setText(textArea.getText() + "\n");
                    }

                    CmdjavafxApplication.getInstance().updateTitle(currPath);

                    break;
                }
                case "cls": {
                    cls = true;
                    textArea.setText("");
                    break;
                }
                case "del": {
                    if (strings.length > 1) {
                        if (strings[1].equals("-a")) {
                            if (strings.length < 3) {
                                textArea.setText(textArea.getText() + "\nНе задан формат документов.");
                            } else {
                                File folder = new File(currPath);
                                File[] listOfFiles = folder.listFiles();

                                if (listOfFiles != null && listOfFiles.length > 0) {
                                    List<File> files = Arrays.stream(listOfFiles)
                                            .filter(File::isFile)
                                            .filter(file -> file.getName().contains(strings[2]))
                                            .collect(Collectors.toList());

                                    if (!files.isEmpty()) {
                                        files.forEach(file -> {
                                            try {
                                                Files.delete(file.toPath());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        });
                                    }
                                } else {
                                    textArea.setText(textArea.getText() + "\nСистеме не удается найти указанный файл.");
                                }
                            }
                        } else {
                            File folder = new File(currPath);
                            File[] listOfFiles = folder.listFiles();

                            if (listOfFiles != null && listOfFiles.length > 0) {
                                if (strings.length > 1 && listOfFiles.length > 1) {
                                    for (int i = 1; i < strings.length; i++) {
                                        int finalI = i;
                                        boolean f = Arrays.stream(listOfFiles).filter(File::isFile).anyMatch(file -> file.getName().equals(strings[finalI]));
                                        if (f) {
                                            Files.delete(new File(strings[finalI]).toPath());
                                        } else {
                                            textArea.setText(textArea.getText() + "\nСистеме не удается найти указанный файл.");
                                        }
                                    }
                                } else {
                                    textArea.setText(textArea.getText() + "\nСистеме не удается найти указанный файл.");
                                }
                            } else {
                                textArea.setText(textArea.getText() + "\nСистеме не удается найти указанный файл.");
                            }
                        }
                    } else {
                        textArea.setText(textArea.getText() + "\n");
                    }

                    break;
                }
                case "dir": {
                    File folder = new File(currPath);
                    File[] listOfFiles = folder.listFiles();

                    textArea.setText(textArea.getText() + "\nСодержимое папки - " + currPath + "\n\n");
                    assert listOfFiles != null;
                    for (File listOfFile : listOfFiles) {
                        String s;
                        if (listOfFile.isFile()) {
                            s = String.format("%-10s%s%30s", "", " ", listOfFile.getName() + "\n");
                            textArea.setText(textArea.getText() + s);
                        } else if (listOfFile.isDirectory()) {
                            s = String.format("%-10s%s%30s", "DIR", " ", listOfFile.getName() + "\n");
                            textArea.setText(textArea.getText() + s);
                        }
                    }

                    break;
                }
                case "help": {
                    if (strings.length == 1) {
                        textArea.setText(textArea.getText() + "\nДля получения сведений об определенной команде наберите HELP <имя команды> \n");
                        commands.forEach((key, value) -> {
                            String s = String.format("%-20s%s-%10s%-40s", key.toUpperCase(), " ", " ", value);
                            textArea.setText(textArea.getText() + "\n" + s + "\n");
                        });
                    } else if (strings.length == 2 && strings[1].equals("params")) {
                        params.forEach((key, value) -> {
                            String s = String.format("%-10s%s-%10s%-40s", key.toUpperCase(), " ", " ", value);
                            textArea.setText(textArea.getText() + "\n" + s + "\n");
                        });
                    } else {
                        textArea.setText(textArea.getText() + "\n");
                    }

                    break;
                }
                case "exit": {
                    System.exit(0);
                    break;
                }
                default: {
                    text = textArea.getText();
                }
            }
        } else if (keyEvent.getCode() == KeyCode.BACK_SPACE || keyEvent.getCode() == KeyCode.DELETE) {
            if (text.length() < textArea.getText().length()) {
                textArea.setText(textArea.getText().substring(0, textArea.getText().length() - 1));
            } else {
                textArea.setText(textArea.getText());
            }

            if (keyEvent.getCode() == KeyCode.DELETE) {
                textArea.positionCaret(textArea.getText().length());
            }
        } else if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.DOWN) {
            if (keyEvent.getCode() == KeyCode.UP) {
                if (indexLastComm > 0) {
                    indexLastComm--;
                } else {
                    indexLastComm = 0;
                }
                textArea.setText(text + listLastComm.get(indexLastComm));
            } else {
                if (indexLastComm <= listLastComm.size()) {
                    indexLastComm++;
                } else {
                    indexLastComm = listLastComm.size();
                }
                if (listLastComm.size() <= indexLastComm) {
                    if (listLastComm.size() <= indexLastComm) {
                        indexLastComm = listLastComm.size();
                    }
                    textArea.setText(text.substring(0, text.lastIndexOf(">") + 1));
                } else {
                    textArea.setText(text + listLastComm.get(indexLastComm));
                }
            }
        }
    }

    public void keyReleased() {
        if (lastCommandIsEnter) {
            lastCommandIsEnter = false;
            if (cls) {
                cls = false;
                textArea.setText(currPath + ">");
            } else {
                textArea.setText(textArea.getText() + "\n" + currPath + ">");
            }
            textArea.positionCaret(textArea.getText().length());
            text = textArea.getText();
        }
        textArea.positionCaret(textArea.getText().length());
    }
}
