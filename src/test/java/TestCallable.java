import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TestCallable {

    public static void main(String[] args) {
        try {
            completionServiceCount();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    /**
     * 使用completionService收集callable结果
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void completionServiceCount() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        CompletionService<Integer> completionService = new ExecutorCompletionService(executorService);
        int threadNum = 5;
        for (int i = 0; i < threadNum; i++) {
            completionService.submit(getTask(i));
        }
        int sum = 0;
        int temp = 0;
        for (int i = 0; i < threadNum; i++) {
            temp = completionService.take().get();
            sum += temp;
            System.out.print(temp + "\t");
        }
        System.out.println("CompletionService all is : " + sum);
        executorService.shutdown();
    }

    public static Callable<Integer> getTask(final int no) {
        final Random rand = new Random();
        Callable<Integer> task = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int time = rand.nextInt(100) * 100;
                System.out.println("thead:" + no + " time is:" + time);
                Thread.sleep(time);
                return no;
            }
        };
        return task;
    }
}