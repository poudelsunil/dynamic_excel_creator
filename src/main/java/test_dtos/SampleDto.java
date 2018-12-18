package test_dtos;

import java.util.List;

public class SampleDto {
    private String name;
    private Integer numericId;
    private List<String> textList;
    private SampleSubClassDto sampleSubClassDto;

    public SampleDto(String name, Integer numericId, List<String> textList, SampleSubClassDto sampleSubClassDto) {
        this.name = name;
        this.numericId = numericId;
        this.textList = textList;
        this.sampleSubClassDto = sampleSubClassDto;
    }
}

