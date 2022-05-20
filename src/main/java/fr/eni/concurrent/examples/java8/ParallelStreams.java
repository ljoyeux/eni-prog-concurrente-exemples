package fr.eni.concurrent.examples.java8;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
//import java.util.stream.Collectors;

/**
 * Created by ljoyeux on 11/06/2017.
 */
public class ParallelStreams {
//    public static void userStreams() {
//        List<Integer> list = new ArrayList<>();
//
//        Random r = new Random(System.currentTimeMillis());
//        for(int i=0; i<1_000_000; i++) {
//            list.add(r.nextInt(1_000_000));
//        }
//
////        list.forEach((i) -> {
////            System.out.println(i);
////        });
//
//
//        long count = list.stream().filter(i -> i > 500_000).collect(Collectors.toList()).stream().count();
//
//        System.out.println(count / (double) list.size());
//    }
//
//    public static void main(String[] args) {
//        userStreams();
//    }
}
