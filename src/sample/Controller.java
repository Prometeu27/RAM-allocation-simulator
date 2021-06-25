package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
//=============INFO============
// Status:
// 0 - proces neinitializat
// 1 - proces activ
// 2 - proces terminat, existent inca in lisa cu procese initializate, in asteptarea reocuparii sau concatenarii
// 3 - proces dezactivat, sters din lista cu procese initializate
//
// Conditii de creare
// 0 - atunci cand este gasit un proces terminat care are memoria egala cu noul proces, si este inlocuit
// 1 - atunci cand este gasit un proces terminat, care are memorie mai multa decat cea a noului proces
// 2 - atunci cand nu exista inca niciun proces initializat
// 3 - atunci cand nu exista niciun proces terminat, dar exista un segment de memorie inca neocupata
//=============================

//========CONTROL PANEL========
    int numberOfProcesses = 1000;
    int maxMemory = 32000;
    int memoryMin = 341, memoryMax = 2048;
    int durationMin = 3, durationMax = 10;
    int startTimeMin = 2, startTimeMax = 25;
//=============================

    int memory, duration, startTime, endTime;
    int x = 0, initializedProcesses = 0, usedMemory = 0, unusedMemory = maxMemory, gasit = 1;
    // variabila pt evidenta timpului, numarul proceselor care au fost active la un moment dat, memoria actuala folosita, memoria actuala nefolosita, variabila care ajuta la ciclare in crearea proceselor
    processes[] proces = new processes[numberOfProcesses]; // array-ul principal de procese
    tableProcesses[] tableProces = new tableProcesses[numberOfProcesses]; // array-ul de procese care apar in tabel
    int[] initializedProcessesArray = new int[numberOfProcesses];

    // variabile informatii consola
    int incheiate= 0;
    int concatenate = 0;
    int tCreate, tEsuate;
    int[] creat = new int[4];
    int[] esuat = new int[3];


    @FXML private TableView<tableProcesses> tableView;
    @FXML private TableColumn<tableProcesses, Integer> indexCol;
    @FXML private TableColumn<tableProcesses, String> nameCol;
    @FXML private TableColumn<tableProcesses, Integer> memoryCol;
    @FXML private TableColumn<tableProcesses, Integer> durationCol;
    @FXML private TableColumn<tableProcesses, Integer> activeCol;
    @FXML private TextFlow console;

    public String randomName() { // metoda pentru generarea unui nume aleator
        Random r = new Random();
        String alphabet = "abcdefghijklmnopqrstuvwxyz";

        final int size = 4; // marimea numelui
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        }
        String randomName = sb.toString();

        return randomName;
    }

    public void createProcesses() { // metoda pentru generarea proceselor, se apeleaza in initialize
        for (int i = 0; i < numberOfProcesses; i++) {
            memory = (int) (Math.random() * (memoryMax - memoryMin + 1) + memoryMin);
            duration = (int) (Math.random() * (durationMax - durationMin + 1) + durationMin);
            startTime = (int) (Math.random() * (startTimeMax- startTimeMin + 1) + startTimeMin);
            endTime = startTime + duration;

            proces[i] = new processes(i, randomName(), memory, startTime, endTime, duration, 0);
            tableProces[i] = new tableProcesses(i, proces[i].getName(), memory, duration, 0);
        }
    }

    public void processError(int i, int errorCode) { // metoda pentru procesarea erorilor si returnarea mesajelor de eroare in consola
        switch (errorCode) {
            case 0: // va fi apelata atunci cand nu mai exista memorie libera pentru un nou proces
                esuat[0]++;
                Text text = new Text(" " + x + ".  Process (" + proces[i].getName() + ") cannot be created, max memory reached (process: " + proces[i].getMemory() + ", actual: " + usedMemory + ", max: " + maxMemory + ").\n");
                text.setFill(Color.RED);
                console.getChildren().add(text);
                break;

            case 1: // va fi apelata atunci cand exista suficienta memorie libera pentru proces, insa nu exista niciun segment de memorie suficient de mare
                esuat[1]++;
                Text text2 = new Text(" " + x + ".  Process (" + proces[i].getName() + ") cannot be created, there was no free memory segment found\n");
                text2.setFill(Color.RED);
                console.getChildren().add(text2);
            break;

            default:
                Text text4 = new Text("Default error.\n");
                text4.setFill(Color.YELLOW);
                console.getChildren().add(text4);
                break;
        }
    }

    public void processCreated(int i, int creat) { // metoda apelata odata cu crearea unui nou proces

        Text text = new Text(" " + x + ".  Process created succesfully ( " + proces[i].getName() + ") memory used: " + proces[i].getMemory() + ", start time: " + proces[i].getStartTime() + "). Condition: " + creat + "\n");
        text.setFill(Color.GREEN); // se anunta crearea procesului in consola
        console.getChildren().add(text);

        proces[i].setStatus(1);
        tableProces[i].setTableStatus(1); // setam procesului statusul 1
        tableView.getItems().add(tableProces[i]); // se adauga procesul in tabel

        initializedProcesses++; // crestem numarul proceselor initializate totale
        unusedMemory -= proces[i].getMemory(); // actualizam memoria nefolosita
        usedMemory += proces[i].getMemory(); // actualizam memoria folosita
        gasit = 1; //  resetam variabila gasit pentru a nu aparea probleme in crearea proceselor viitoare
    }

    public void initializedShifting (int i, int j) { // metoda care shifteaza elementele variabilei ce contine indexul fiecarui proces initializat
        for (int k = initializedProcesses; k >= j; k--) {
            if (k > j) {
                initializedProcessesArray[k] = initializedProcessesArray[k - 1]; // facem loc in array pentru noul proces
            }
        }
        initializedProcessesArray[j + 1] = proces[i].getIndex(); // adaugam index-ul noului proces in array, in coada segmentului de memorie libera gasit
    }

    public void concatenateMemory() { // metoda pentru concatenarea segmentelor de memorie libera ramase in urma terminarii unelor procese
        tCreate = creat[0] + creat[1] + creat[2] + creat[3];
        tEsuate = esuat[0] + esuat[1];

        for (int i = 0; i < initializedProcesses; i++) {
            if(proces[initializedProcessesArray[i]].getStatus() == 2 && proces[initializedProcessesArray[i + 1]].getStatus() == 2) { // parcurgem toate procesele din lista de initializate, si verificam daca procesele vecine pot fi concatenate
                proces[initializedProcessesArray[i + 1]].setStatus(3);
                tableProces[initializedProcessesArray[i + 1]].setTableStatus(3); // setam procesului statusul 3
                proces[initializedProcessesArray[i]].setMemory(proces[initializedProcessesArray[i]].getMemory() + proces[initializedProcessesArray[i + 1]].getMemory()); // concatenam memoria
                proces[initializedProcessesArray[i + 1]].setMemory(0); // setam ca fiind 0 memoria procesului secundar
                tableView.refresh();

                concatenate++; // crestem variabila pentru supravegherea proceselor concatenate
                Text textConcatenate = new Text(" "+ x + ". " + "Processes concatenated. Indexes: " + initializedProcessesArray[i] + ", " + initializedProcessesArray[i + 1] + ".\n");
                textConcatenate.setFill(Color.PURPLE);
                console.getChildren().add(textConcatenate); // anuntam in consola actiunea efectuata

                for (int j = i + 1; j < initializedProcesses - 1; j++) { // eliminam din consola indexul secundar
                    initializedProcessesArray[j] = initializedProcessesArray[j + 1];
                    initializedProcessesArray[j + 1] = 0;
                }
                if(((concatenate + 1) + tEsuate) == numberOfProcesses) { // eliminam ultimul proces ramas neconcatenat si aducem memoria la valoarea initiala
                    unusedMemory = maxMemory;
                    proces[initializedProcessesArray[0]].setMemory(0);
                    proces[initializedProcessesArray[0]].setStatus(3);
                    proces[initializedProcessesArray[1]].setStatus(3);
                    proces[initializedProcessesArray[2]].setStatus(3);
                    initializedProcessesArray[0] = 0;
                }
                if(((concatenate + 2) + tEsuate) == numberOfProcesses) {
                    unusedMemory = maxMemory;
                    proces[initializedProcessesArray[0]].setStatus(3);
                    proces[initializedProcessesArray[1]].setStatus(3);
                    proces[initializedProcessesArray[2]].setStatus(3);
                    Arrays.fill(initializedProcessesArray, 0);

                }
            }
        }

    }
    public void checkTime(int x) { // metoda care ruleaza in fiecare secunda, bazata pe incrementarea variabilei x ce contorizeaza timpul

        for (int i = 0; i < numberOfProcesses; i++) { // in fiecare secunda, programul verifica toate procesele create
            if (proces[i].getStartTime() == x && (usedMemory + proces[i].getMemory()) < maxMemory) {  // verifica daca startTime-ul unuia dintre procese
                // este egal cu X-ul (timpul actual dat de timer)
                // si exista suficienta memorie totala libera
                if (initializedProcesses != 0) {
                    outerloop:
                    for (int j = 0; j < initializedProcesses; j++) {
                        // se parcurge toata lista cu procesele deja initializate - active sau terminate
                        if (proces[initializedProcessesArray[j]].getStatus() == 2) {
                            if (proces[initializedProcessesArray[j]].getMemory() >= proces[i].getMemory()) { // daca noul proces are memoria egala cu vechiul proces | conditia de creare nr. 0
                                // se verifica daca se gaseste un proces care e terminat si are memorie cel putin egala cu noul proces
                                if (proces[initializedProcessesArray[j]].getMemory() == proces[i].getMemory()) { // se verifica daca procesul gasit are memoria egala cu cel nou
                                    initializedProcessesArray[j] = proces[i].getIndex(); // inlocuim index-ul procesului vechi din array, cu indexul nou
                                    creat[0]++;
                                    processCreated(i, 0);
                                    break outerloop; // odata creat procesul, iesim din loop
                                } else if (proces[initializedProcessesArray[j]].getMemory() > proces[i].getMemory()) { // daca procesul vechi are mai multa memorie decat cel nou | conditia de creare nr. 1
                                    initializedShifting(i, j);
                                    proces[initializedProcessesArray[j]].setMemory(proces[initializedProcessesArray[j]].getMemory() - proces[i].getMemory());
                                    // scadem memoria noului proces, din memoria procesului vechi
                                    processCreated(i, 1);
                                    creat[1]++;
                                    break outerloop;
                                }
                            } else gasit = 0;

                        } else gasit = 0; // daca niciuna din cele doua conditii anterioare nu sunt indeplinite, setam gasit ca fiind 0
                    }
                } else if (initializedProcesses == 0) { // daca initializedProcesses == 0 - nu a fost inca initializat niciun proces | conditia de creare nr. 2
                    initializedProcessesArray[initializedProcesses] = proces[i].getIndex();
                    processCreated(i, 2);
                    creat[2]++;
                }

                if (gasit == 0 && proces[i].getMemory() < unusedMemory) { // daca gasit a fost setat anterior ca fiind 0, si exista un segment de memorie nefolosit vreodata | conditia de creare nr. 3
                    initializedProcessesArray[initializedProcesses] = proces[i].getIndex();
                    processCreated(i, 3);
                    creat[3]++;

                 } else if (gasit == 0 && proces[i].getMemory() > unusedMemory) processError(i, 1); // daca a doua conditie a if-ului anterior nu e indeplinita, intoarcem eroare

            } else if ((usedMemory + proces[i].getMemory()) > maxMemory && proces[i].getStartTime() == x) processError(i, 0); // daca memoria totala rezultata in urma crearii procesului ar depasi memoria totala, intoarcem eroare


            if (proces[i].getEndTime() == x && proces[i].getStatus() == 1) { // verificam daca end time-ul unuia din procese este gasit, iar procesul este activ | metoda pt incheierea proceselor
                tableProces[i].setTableStatus(2);
                proces[i].setStatus(2); // setam statusul procesului ca fiind incheiat
                tableView.refresh(); // actualizam tabelul

                if (usedMemory - proces[i].getMemory() > 0) usedMemory -= proces[i].getMemory(); // scadem memoria procesului din memoria totala
                else usedMemory = 0;

                Text text = new Text(" " + x + ".  Process ended (" + proces[i].getName() + "). Start time: " + proces[i].getStartTime() + ", end time: " + proces[i].getEndTime() + ", duration: " + proces[i].getDuration() + "\n");
                text.setFill(Color.BLUE);
                console.getChildren().add(text); // afisam incheierea procesului in consola
                incheiate++;
            }
        }
        concatenateMemory(); // apelam concatenarea proceselor

        // statisticile afisate in consola
        System.out.println("=======" + "\nCreate: " + creat[0] + " " + creat[1] + " " + creat[2] + " " + creat[3] + " | " + (creat[0] + creat[1] + creat[2] + creat[3]) +"\n" + "Esuat: " + esuat[0] + " " + esuat[1] + " | " + (esuat[0] + esuat[1]) + "\nIncheiate:" + incheiate + "\nConcatenari: " + concatenate + "\nUsed memory: " + usedMemory + "\nUnused memory: " + unusedMemory);
        System.out.println("Procese initializate: " + Arrays.toString(initializedProcessesArray));
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        tableView.setPlaceholder(new Label("No processes created yet.")); // initializam tabelul
        indexCol.setCellValueFactory(new PropertyValueFactory<tableProcesses, Integer>("tableIndex"));
        nameCol.setCellValueFactory(new PropertyValueFactory<tableProcesses, String>("tableName"));
        memoryCol.setCellValueFactory(new PropertyValueFactory<tableProcesses, Integer>("tableMemory"));
        durationCol.setCellValueFactory(new PropertyValueFactory<tableProcesses, Integer>("tableDuration"));
        activeCol.setCellValueFactory(new PropertyValueFactory<tableProcesses, Integer>("tableStatus"));

        if (x <= numberOfProcesses) createProcesses(); // cream procesele

        Timer timer = new java.util.Timer(); // timer-ul care se incrementeaza in fiecare secunda
        timer.schedule(new TimerTask() {
            public void run() {
                Platform.runLater(new Runnable() {
                    public void run() {
                        checkTime(x);
                        x++;

                    }
                });
            }
        }, 0, 1000);
    }
}