package com.youlexuan.group;

import com.youlexuan.pojo.TbSpecification;
import com.youlexuan.pojo.TbSpecificationOption;

import java.io.Serializable;
import java.util.List;

public class Specification implements Serializable {

    private TbSpecification specification;
    private List<TbSpecificationOption> specificationOptionList;

    public TbSpecification getSpecification() {
        return specification;
    }

    public void setSpecification(TbSpecification specification) {
        this.specification = specification;
    }

    public List<TbSpecificationOption> getSpecificationOptionList() {
        return specificationOptionList;
    }

    public void setSpecificationOptionList(List<TbSpecificationOption> specificationOptionList) {
        this.specificationOptionList = specificationOptionList;
    }
}
