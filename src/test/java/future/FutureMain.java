package future;

import java.util.concurrent.*;

public class FutureMain {

    static ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        run();
        run();
        run();
        executorService.shutdown();
    }

    static void run() throws ExecutionException, InterruptedException {
        FutureTask<String> futureTaska = new FutureTask<>(new RealData("A", 5000));
        FutureTask<String> futureTaskb = new FutureTask<>(new RealData("B", 10000));

        executorService.execute(futureTaska);
        executorService.execute(futureTaskb);

        System.out.println("请求完毕！");

        try {
            Thread.sleep(2000);
            System.out.println("这里经过了一个2秒的操作！");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("真实数据 B：" + futureTaskb.get());
        System.out.println("真实数据 A：" + futureTaska.get());

    }
}
class RealData implements Callable<String> {

    private String result;

    private long sleep;

    public RealData(String result, long sleep) {
        this.result = result;
        this.sleep = sleep;
    }

    @Override
    public String call() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(result);
        //模拟耗时的构造数据过程
        Thread.sleep(sleep);
        return sb.toString();
    }
}