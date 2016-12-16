package services.proxy;

import org.jetbrains.annotations.NotNull;
import util.FileUtil;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


public class PortScanner {
    public static final int CALLBACK_EXECUTOR_SIZE = 10;
    public static final int SCAN_EXECUTOR_SIZE = 20;
    public static final int SCAN_EXECUTOR_TIMEOUT = 200;
    public static final String DEFAULT_CACHE_FILE_PATH = "87hv4rc21.txt";

    public static final int STATE_SCANNING = 1;
    public static final int STATE_WAITING = 3;

    public static final int PORT_VALUE_MIN = 0x400;
    public static final int PORT_VALUE_MAX = 7000;//0xFFFF;

    public String cachePath;
    public String address;

    private ExecutorService scanExecutor;
    private ExecutorService callbackExecutor;
    private List<Integer> availablePorts;

    private List<Callable<Void>> callbacks;

    private int state;

    public PortScanner(@NotNull String address, String cachePath) {
        this.cachePath = FileUtil.absolute(cachePath == null ? DEFAULT_CACHE_FILE_PATH : cachePath);
        this.address = address;

        callbackExecutor = Executors.newFixedThreadPool(CALLBACK_EXECUTOR_SIZE);
        scanExecutor = Executors.newFixedThreadPool(SCAN_EXECUTOR_SIZE);

        availablePorts = new LinkedList<>();
        callbacks = new ArrayList<>();
        state = STATE_WAITING;

        FileUtil.createFileIfAbsent(this.cachePath);
        fetchCache();
    }

    public void runScanning() throws InterruptedException, ExecutionException {
        if (state == STATE_SCANNING) {
            return;
        }

        state = STATE_SCANNING;
        availablePorts.clear();
        final List<Future<Integer>> futures = new ArrayList<>();
        for (int port = PORT_VALUE_MIN; port <= PORT_VALUE_MAX; port++) {
            futures.add(portIsAvailable(scanExecutor, address, port, SCAN_EXECUTOR_TIMEOUT));
        }

        scanExecutor.awaitTermination(SCAN_EXECUTOR_TIMEOUT, TimeUnit.MILLISECONDS);
        for (final Future<Integer> f : futures) {
            if (f.get() != null) {
                availablePorts.add(f.get());
            }
        }

        terminate();
    }

    public PortScanner stopExecution() {
        if (state == STATE_SCANNING) {
            scanExecutor.shutdownNow();
            callbackExecutor.shutdownNow();
            state = STATE_WAITING;
        }

        return this;
    }

    public PortScanner addCallback(Callable<Void> c) {
        callbacks.add(c);

        return this;
    }

    public PortScanner clearCallbacks() {
        callbacks.clear();

        return this;
    }

    public int getState() {
        return this.state;
    }

    public boolean isScanning() {
        return this.state == STATE_SCANNING;
    }

    private void terminate() throws InterruptedException {
        List<Callable<Void>> scanTerminateTask = new ArrayList<>();
        scanTerminateTask.add(() -> {
            writeCache();
            state = STATE_WAITING;
            callbackExecutor.invokeAll(callbacks);
            scanExecutor.shutdownNow();
            return null;
        });

        List<Callable<Void>> callbackTerminateTask = new ArrayList<>();
        callbackTerminateTask.add(() -> {
            callbackExecutor.shutdownNow();
            return null;
        });

        callbackExecutor.invokeAll(callbackTerminateTask);
        scanExecutor.invokeAll(scanTerminateTask);
    }

    private void fetchCache() {
        if (!FileUtil.checkFile(cachePath)) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(cachePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                availablePorts.add(Integer.parseInt(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeCache() {
        if (!FileUtil.checkFile(cachePath)) {
            return;
        }

        try {
            PrintWriter writer = new PrintWriter(cachePath, "UTF-8");
            availablePorts.forEach(writer::println);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getAvailablePorts() {
        return availablePorts;
    }

    private static Future<Integer> portIsAvailable(
        final ExecutorService es,
        final String ip,
        final int port,
        final int timeout
    ) {
        return es.submit(() -> {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(port);
                ss.setReuseAddress(true);
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), timeout);
                socket.close();
                return port;
            } catch (IOException ignored) {
            } finally {
                if (ss != null) {
                    try {
                        ss.close();
                    } catch (IOException ignored) {}
                }
            }

            return null;
        });
    }
}
