package arrays;

import java.util.*;

public class Array {

    public boolean containsDuplicate() {
        int[] nums = new int[]{1, 2, 3, 3};
        Set<Integer> tracker = new HashSet<>();
        for(int i = 0; i < nums.length; i++) {
            if (tracker.contains(nums[i])) { return true; };
            tracker.add(nums[i]);
        }
        return false;
    }

    public boolean areAnagrams() {
        String s = "racecar", t = "carrace";
        int[] chars = new int[26];
        if(s.length() != t.length()) {
            return false;
        }
        for(int i = 0; i < s.length(); i++) {
            chars[s.charAt(i) - 'a']++;
            chars[t.charAt(i) - 'a']--;
        }

        for(int i : chars) {
            if (i != 0) {
                return false;
            }
        }

        return true;
    }

    public int[] twoSum() {
        int[] nums = new int[] {3,4,5,6};
        int target = 7;
        int left = 0;
        int right = nums.length - 1;

        while(left < right) {
            if(nums[left] + nums[right] == target) {
                return new int[] { left, right };
            }
            if(nums[left] + nums[right] > target) {
                right--;
            } else {
                left++;
            }
        }
        return new int[]{};

    }

    public List<List<String>> anagramGroup() {
        String[] input = new String[] {"act","pots","tops","cat","stop","hat"};
        HashMap<String, List<String>> result = new HashMap<>();
        for (int i = 0; i < input.length; i++) {
            int[] counter = new int[26];
            for(int j = 0; j < input[i].length(); j++) {
                counter[input[i].charAt(j) - 'a']++;
            }
            String wordSequence = Arrays.toString(counter);
            result.putIfAbsent(wordSequence, new ArrayList<>());
            result.get(wordSequence).add(input[i]);
        }
        return new ArrayList<>(result.values());
    }

    public int[] topKElements() {
        int[] nums = new int[] {1,2,2,3,3,3 };
        int k = 2;
        int[] result = new int[k];
        Map<Integer, Integer> freqCounter = new HashMap<>();
        for(int i = 0; i < nums.length; i++) {
            freqCounter.put(nums[i], freqCounter.getOrDefault(nums[i], 0) + 1);
        }

        PriorityQueue<Map.Entry<Integer, Integer>> queue = new PriorityQueue<>((a ,b) -> b.getValue() - a.getValue());
        for(Map.Entry entry : freqCounter.entrySet()) {
            queue.offer(entry);
        }

        int counter = 0;
        while(!queue.isEmpty()) {
            if(counter >= k) { break;}
            result[counter] = queue.poll().getKey();
            counter++;
        }

        return result;
    }


    public int[] productExceptSelf() {
        int[] nums = new int[] { 4,3,2,1,2 };
        int[] prefix = new int[nums.length];
        int[] postfix = new int[nums.length];
        int[] result = new int[nums.length];

        prefix[0] = nums[0];
        for(int i = 1; i < nums.length; i++) {
            prefix[i] = prefix[i - 1] * nums[i];
        }

        postfix[nums.length - 1] = nums[nums.length - 1];
        for(int i = nums.length - 2; i >= 0; i--) {
            postfix[i] = postfix[i + 1] * nums[i];
        }

        int fallback = 1;
        for(int i = 0; i < nums.length; i++) {
            if(i == 0) {
                result[i] = fallback * postfix[i + 1];
            } else if (i == nums.length - 1) {
                result[i] = prefix[i - 1] * fallback;
            } else {
                result[i] = prefix[i - 1] * postfix[i + 1];
            }

        }

        return result;
    }

    public static void main(String[] args) {
        Array ar = new Array();
        System.out.println(ar.containsDuplicate());
        System.out.println(ar.areAnagrams());
        System.out.println(ar.productExceptSelf());
    }

}
