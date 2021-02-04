//UT-EID=


import java.util.*;
import java.util.concurrent.*;


public class PMerge extends Thread{
  int[] A;
  int[] B;
  int[] C;
  int index;

  public PMerge(int[] A, int[] B, int[] C, int index){
    this.A = A;
    this.B = B;
    this.C = C;
    this.index = index;
  }

  public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){
    try{      
      ExecutorService exec = Executors.newFixedThreadPool(numThreads);
      
      for (int i = 0; i < A.length + B.length; i++){
        PMerge merge = new PMerge(A, B, C, i);
        exec.execute(merge);
      }
      
      exec.shutdown();
      exec.awaitTermination(5, TimeUnit.SECONDS); // TA said that 30 sec is fine, should be v fast tho
    }
    catch (Exception e){
      System.out.println(e);
    }
  }

  @Override
  /**
   * 
   */
  public void run() {
    if (index < 0 || index >= C.length)
      return;

    int[] arr = index < A.length ? A : B; //array that index belongs to
    int[] oppositeArr = arr == A ? B : A; //need to find position here
    int shiftIndex = index < A.length ? index : index - A.length;

    //check if oppositeArr is empty
    if (oppositeArr.length == 0){
      int newIndex = C.length - 1 - shiftIndex;
      C[newIndex] = arr[shiftIndex];
      return;
    }

    int oppIndex = binarySearch(arr[shiftIndex], oppositeArr);
    int newIndex = C.length - 1 - shiftIndex - oppIndex; //invert index for descending order
    C[newIndex] = arr[shiftIndex];
  }

  /**
   * Finds the index of value to be placed at in searchArray
   * @param value value to find the index of in searchArray
   * @param searchArray ascending sorted array
   * @return index of value in relation to searchAray's elements
   */
  public int binarySearch(int value, int[] searchArray){
    int left = 0;
    int right = searchArray.length;
    int oppIndex = (left + right) / 2;  //index of where an element should be if in ascending order
    int shift = searchArray == B ? A.length : 0;

    while (left <= right){
      if (oppIndex + 1 >= searchArray.length){ //if searchArray[oppIndex] is last element of array
        if (searchArray[oppIndex] < value)
          return searchArray.length;
        else if (searchArray[oppIndex] > value)
          right = oppIndex - 1;
        else { //equal to the last element of an array
          if (index > shift + oppIndex)
            return oppIndex + 1;
          return oppIndex;
        }
      }      
      else if (searchArray[oppIndex] == value){ //if A and B share a value
        if (index > shift + oppIndex)
          return oppIndex + 1;
        return oppIndex;
      }
      else if (searchArray[oppIndex + 1] == value) //if A and B share a value
        return index < shift + oppIndex + 1 ? oppIndex + 1 : oppIndex + 2;
      else if (searchArray[oppIndex] < value && value < searchArray[oppIndex + 1])
        return oppIndex + 1;
      else if (searchArray[oppIndex] > value)
        right = oppIndex - 1;
      else if (searchArray[oppIndex + 1] < value)
        left = oppIndex + 1;
      
      oppIndex = (left + right) / 2;
    }

    return oppIndex;
  }
}
