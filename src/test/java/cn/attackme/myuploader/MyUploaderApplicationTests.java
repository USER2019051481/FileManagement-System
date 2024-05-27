package cn.attackme.myuploader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class MyUploaderApplicationTests {

    @Test
    public void contextLoads() {
//        LinkedList<Integer> link = new LinkedList<>() ;
//        ArrayList<Integer> array = new ArrayList<>() ;
//         array.size() ;
//        System.out.println();
//        Integer first = link.getFirst();
//        Integer pop = link.pop();
//        System.out.println( Math.pow(2,3) );
//        link.clear();
//        Integer pop1 = link.pop();
//
//
//        array.clear();
//
//        String s = String.valueOf(1);
//        StringBuilder sb = new StringBuilder(s ) ;
//        sb.append(Integer.toString(1)) ;
//        List<String> list = new ArrayList<>() ;
//        list.add(s) ;

//        int cut = 1 ;
//        int[] postorder = new int[cut];
//        int min = Arrays.stream(postorder).min().getAsInt();
//
//        int[] inorder = new int[0];
//        System.out.println(inorder);
//        List<Integer> list = new ArrayList<>() ;

//        Map<Integer,Integer> map = new HashMap<>() ;
//        map.put(1,200) ;
//        map.put(3,100) ;
//        map.put(2,300) ;
//        map.put(4,300) ;
//        System.out.println(map.get(3)) ;
//        System.out.println(map.get(5)) ;
//        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
//            System.out.println(entry.getKey());
//
////            System.out.println(entry.getValue());
//        }
        Queue<Integer> qu = new LinkedList<>() ;
        qu.add(4) ;
        qu.add(2) ;
        qu.add(3) ;
        System.out.println(qu.poll());
        System.out.println(qu.poll());
        System.out.println(qu.poll());


    }

}
