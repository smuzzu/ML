package com.ml.utils;

import java.io.File;
import java.time.LocalTime;

public class SData {

    public static LocalTime NON_WORKING_HOURS_FROM = LocalTime.of(0,01);
    public static LocalTime NON_WORKING_HOURS_FROM2 = LocalTime.of(21,30);
    public static LocalTime NON_WORKING_HOURS_TO = LocalTime.of(7,00);
    public static LocalTime NON_WORKING_HOURS_TO2 = LocalTime.of(9,30);
    public static double LIMITE_MONO_AFIP=460000;

    private static String QUEFRESQUETE="RWHJWKZY^O_Q";
    private static String idClienteQUEFRESQUETE = "8794<<=9";
    private static String appIdQUEFRESQUETE = "24<6=<@8:ACBCEFA";
    private static String appSecretQUEFRESQUETE = "mT3=fN:<_c\"\\'e#_afIanN*kp\"%7p''U";
    private static String tokenFileQUEFRESQUETE="C:"+ File.separator+"centro"+File.separator+"tokenSeba.txt";

    public static String getQuefresquete(){
        return decode(QUEFRESQUETE);
    }

    public static String getIdClienteQuefresquete() {
        return decode(idClienteQUEFRESQUETE);
    }

    public static String getAppIdQuefresquete(){
        return decode(appIdQUEFRESQUETE);
    }

    public static String getAppSecretQuefresquete(){
        return decode(appSecretQUEFRESQUETE);
    }

    public static String getTokenFileQuefresquete() {
        String value=tokenFileQUEFRESQUETE;
        if (File.separator.equals("/")){
            value="shutdown.bat";
        }
        return value;
    }




    private static String ACACIAYLENGA="BEDGNG`TNXRM";
    private static String idClienteACACIAYLENGA = "364;:7>A?";
    private static String appIdACACIAYLENGA = "597;;=9?9?;C=@FB";
    private static String appSecretACACIAYLENGA = "vkJ85n~M_:=e\"}Guu|V|M*.H%|O^fe:U";
    private static String tokenFileACACIAYLENGA="C:"+File.separator+"centro"+File.separator+"tokenAcacia.txt";

    public static String getAcaciaYLenga(){
        return decode(ACACIAYLENGA);
    }

    public static String getIdClienteAcaciaYLenga() {
        return decode(idClienteACACIAYLENGA);
    }

    public static String getAppIdAcaciaYLenga(){
        return decode(appIdACACIAYLENGA);
    }

    public static String getAppSecretAcaciaYLenga(){
        return decode(appSecretACACIAYLENGA);
    }

    public static String getTokenFileAcaciaYLenga() {
        String value = tokenFileACACIAYLENGA;
        if (File.separator.equals("/")){
            value="startup.bat";
        }
        return value;
    }

    private static String SOMOS_MAS="TQPSXeTI\\";
    private static String idClienteSOMOS_MAS = "7987<99<";
    private static String appIdSOMOS_MAS = "8:6:887:B:DDAD@I";
    private static String appSecretSOMOS_MAS = "boPW6STi^KrPgRtUdb[%kG~&$'su~,$j";
    private static String tokenFileSOMOS_MAS="C:"+File.separator+"centro"+File.separator+"tokenSomosMas.txt";

    public static String getSomosMas(){
        return decode(SOMOS_MAS);
    }

    public static String getIdClienteSomosMas() {
        return decode(idClienteSOMOS_MAS);
    }

    public static String getAppIdSomosMas(){
        return decode(appIdSOMOS_MAS);
    }

    public static String getAppSecretSomosMas(){
        return decode(appSecretSOMOS_MAS);
    }

    public static String getTokenFileSomosMas() {
        String value = tokenFileSOMOS_MAS;
        if (File.separator.equals("/")){
            value="version.bat";
        }
        return value;
    }

    private static String MARIANA_TEST="ncumfth\\n}\u007F";
    public static String getMarianaTest(){
        return decode(MARIANA_TEST);
    }


    private static String globalCloudUrl = "kfeg?vv{}q}q!\u007F{J@At'(&~y(/(6J\",*/%-3-S,.+726A7]53G5afdML?kiQCLBPoLS~zzzzx[[`bVbVe";
    private static String globalCloudUser = "qqvxlxl{";
    private static String globalCouldPassword = "OgzXjsiwYk~\u007F%}\"tBDFHJ";

    public static String getGlobalCloudUrl(){
        return decode(globalCloudUrl);
    }

    public static String getGlobalCloudUser(){
        return decode(globalCloudUser);
    }

    public static String getGlobalCouldPassword(){
        return decode(globalCouldPassword);
    }


    private static final String mailUsername = "cgdxwo\"lj}~ur$Ow~s|!Cy'&";
    private static final String mailPassword = "txewpumzoxq{!ztv";
    private static final String mailAddressList = "tgeer{\"#~<Kszox|?u#\"Ay|'.-+\"/4)1#0-*4;7<i.1;BA?6CH=E7DA>HOKPkBONoDV";
    private static final String mailAddressList2 = "tgeer{\"#~<Kszox|?u#\"A$~!&HMMMP_(/$-1S*76";
    private static final String mailAddressList3 = "tgeer{\"#~<Kszox|?u#\"A*x'.$|$-,%%+1%U^_\\h18-6:\\3@?";
    private static final String mailErrorNotification = "tgeer{\"#~Jrynw{>t\"!";

    public static String getMailUsername(){
        return decode(mailUsername);
    }

    public static String getMailPassword(){
        return decode(mailPassword);
    }

    public static String getMailAddressList(){
        return decode(mailAddressList);
    }

    public static String getMailAddressList2(){
        return decode(mailAddressList2);
    }

    public static String getMailAddressList3(){
        return decode(mailAddressList3);
    }

    public static String getMailErrorNotification(){
        return decode(mailErrorNotification);
    }

    private static String hostname1="EGVOYUW5;ZXN=bb";
    private static String hostname2="Dkfptt";
    private static String hostname3="Skyeig}qj>";
    private static String hostname4="jr05<84;:7=A:B";

    public static String getHostname1(){
        return decode(hostname1);
    }

    public static String getHostname2(){
        return decode(hostname2);
    }

    public static String getHostname3(){
        return decode(hostname3);
    }

    public static String getHostname4(){
        return decode(hostname4);
    }

    public static String encode(String str){
        char[] charArray=str.toCharArray();
        int z=0;
        for (int i=0; i<charArray.length; i++){
            z++;
            if (z==96){
                z=1;
            }
            int newValue = charArray[i]+z;
            if (newValue>127){
                newValue-=95;
            }
            charArray[i]=(char)newValue;
        }
        String result=String.valueOf(charArray);
        return result;
    }

    public static String decode(String str){
        char[] charArray=str.toCharArray();
        int z=0;
        for (int i=0; i<charArray.length; i++){
            z++;
            if (z==96){
                z=1;
            }
            int newValue = charArray[i]-z;
            if (newValue<32){
                newValue+=95;
            }
            charArray[i]=(char)newValue;
        }
        String result=String.valueOf(charArray);
        return result;
    }


    public static void main(String args[]){

        System.out.println(getMailAddressList3());

        boolean b=false;
        System.out.println(hostname4);
        System.out.println(encode(hostname4));
        System.out.println(decode(hostname4));
        System.out.println(decode("jr05<84;:7=A:B"));



    }

}
