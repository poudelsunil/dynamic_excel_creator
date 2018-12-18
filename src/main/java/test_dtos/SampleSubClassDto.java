package test_dtos;

import java.util.Set;

public class SampleSubClassDto {
    int a;
    String txt;
    Set<Test> myset;
    Samaple2Sub sub2Class;

    public SampleSubClassDto(int a, String txt, Set<Test> myset, Samaple2Sub sub2Class) {
        this.a = a;
        this.txt = txt;
        this.myset = myset;
        this.sub2Class = sub2Class;
    }

    public static class Test{
        int n;
        String nestedListObj;

        public Test(int n, String nestedListObj) {
            this.n = n;
            this.nestedListObj = nestedListObj;
        }
    }
}
