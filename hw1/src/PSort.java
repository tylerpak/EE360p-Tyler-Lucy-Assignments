//UT-EID= tjp2365, <Lucy's EID>


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class PSort {

  public static void parallelSort(int[] A, int begin, int end) {
    // TODO: Implement your parallel sort function
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    MyRecursiveAction thread = new MyRecursiveAction(A, begin, end);
    forkJoinPool.execute(thread);
    while(!thread.isDone()) {
      forkJoinPool.shutdown();
      try {
        forkJoinPool.awaitTermination(5, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public static class MyRecursiveAction extends RecursiveAction {
    private static int THRESHOLD = 16;
    private int[] A;
    private int begin;
    private int end;
    private int workload;

    public MyRecursiveAction(int[] A, int begin, int end) {
        this.A = A;
        this.begin = begin;
        this.end = end - 1;
        this.workload = end - begin + 1;
    }

    public static void sort(int[] A, int begin, int end) {
      if(A.length <= THRESHOLD) {
        insertSort(A, begin, end);
      }
      else if (begin < end) {
        int pi = part(A, begin, end);
        parallelSort(A, begin, pi - 1);
        parallelSort(A, pi + 1, end);
      }
    }

    public static void insertSort(int[] A, int begin, int end) {
      for (int i = begin+1; i < end+2; ++i) {
        int val = A[i];
        int j = i - 1;
        while (j >= 0 && A[j] > val) {
          A[j + 1] = A[j];
          j = j - 1;
        }
        A[j + 1] = val;
      }
    }

    public static int part(int[] A, int begin, int end) {
      int pi = A[end];
      int index = (begin - 1);
      for (int i = begin; i < end; i++) {
        if (A[i] < pi) {
          index++;
          int temp = A[index];
          A[index] = A[i];
          A[i] = temp;
        }
      }
      int temp = A[index + 1];
      A[index + 1] = A[end];
      A[end] = temp;

      return index + 1;
    }

    @Override
    protected void compute() {
      if(workload <= 16) {
        insertSort(A, begin, end);
      }
      else if (begin < end) {
        int pi = A[end];
        int index = (begin - 1);
        for (int i = begin; i < end; i++) {
          if (A[i] < pi) {
            index++;
            int temp = A[index];
            A[index] = A[i];
            A[i] = temp;
          }
        }
        int temp = A[index + 1];
        A[index + 1] = A[end];
        A[end] = temp;
        pi = index + 1;
        MyRecursiveAction task1 = new MyRecursiveAction(A, begin, pi - 1);
        task1.fork();
        MyRecursiveAction task2 = new MyRecursiveAction(A, pi + 1, end);
        task2.fork();
      }
    }
  }
}