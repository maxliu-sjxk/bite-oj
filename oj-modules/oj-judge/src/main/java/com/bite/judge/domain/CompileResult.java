package com.bite.judge.domain;

import lombok.Data;
import lombok.Getter;

@Data
public class CompileResult {

    private boolean compiled;  //编译是否成功

    private String exeMessage;  //编译输出信息 （错误信息）

}