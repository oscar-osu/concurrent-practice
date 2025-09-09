import java.util.*;

public class LeetCode {
    public LeetCode(){

    }

    public LeetCode(int a,int b,int c,int d,int e){


    }

    public static void bucketSort(float[] arr) {
        if (arr.length == 0) return;

        // 1. 创建桶
        int numBuckets = 10;
        List<Float>[] buckets = new List[numBuckets];
        for (int i = 0; i < numBuckets; i++) {
            buckets[i] = new ArrayList<>();
        }

        // 2. 放入桶
        for (float num : arr) {
            int index = (int) (num * numBuckets); // 假设输入在 [0,1)
            buckets[index].add(num);
        }

        // 3. 每个桶内排序
        for (List<Float> bucket : buckets) {
            Collections.sort(bucket);
        }

        // 4. 拼接结果
        int idx = 0;
        for (List<Float> bucket : buckets) {
            for (float num : bucket) {
                arr[idx++] = num;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        float[] arr = {0.78f, 0.17f, 0.39f, 0.26f, 0.72f, 0.94f, 0.21f, 0.12f, 0.23f, 0.68f};
        System.out.println("原数组: " + Arrays.toString(arr));
        bucketSort(arr);
        System.out.println("桶排序后: " + Arrays.toString(arr));
        System.out.println(9796);
    }
}
