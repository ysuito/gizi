package com.example.gizi;

import org.jetbrains.annotations.NotNull;

class SecretString {

   @NotNull
   static public String getAPIKey() {
        return (new Object() {
            int t;
            public String toString() {
                byte[] buf = new byte[64];
                t = 1775036390;buf[0] = (byte) (t >>> 14);
                t = -908301140;buf[1] = (byte) (t >>> 5);
                t = -762554283;buf[2] = (byte) (t >>> 6);
                t = -308672910;buf[3] = (byte) (t >>> 1);
                t = 1308623650;buf[4] = (byte) (t >>> 3);
                t = 949545880;buf[5] = (byte) (t >>> 15);
                t = -1677913919;buf[6] = (byte) (t >>> 2);
                t = 328555038;buf[7] = (byte) (t >>> 20);
                t = -325930230;buf[8] = (byte) (t >>> 7);
                t = 1893616503;buf[9] = (byte) (t >>> 18);
                t = 337022363;buf[10] = (byte) (t >>> 2);
                t = 1788227786;buf[11] = (byte) (t >>> 8);
                t = -1147227523;buf[12] = (byte) (t >>> 7);
                t = -1716103006;buf[13] = (byte) (t >>> 12);
                t = -1747220919;buf[14] = (byte) (t >>> 4);
                t = -438750734;buf[15] = (byte) (t >>> 8);
                t = -767032419;buf[16] = (byte) (t >>> 4);
                t = 816231769;buf[17] = (byte) (t >>> 23);
                t = -894898977;buf[18] = (byte) (t >>> 2);
                t = -973236825;buf[19] = (byte) (t >>> 10);
                t = 1123780144;buf[20] = (byte) (t >>> 5);
                t = 1667461280;buf[21] = (byte) (t >>> 12);
                t = 428362472;buf[22] = (byte) (t >>> 23);
                t = 1863188223;buf[23] = (byte) (t >>> 14);
                t = -67426208;buf[24] = (byte) (t >>> 1);
                t = -1711945027;buf[25] = (byte) (t >>> 23);
                t = -768397643;buf[26] = (byte) (t >>> 15);
                t = 1612927634;buf[27] = (byte) (t >>> 12);
                t = 1395944252;buf[28] = (byte) (t >>> 19);
                t = -1119590980;buf[29] = (byte) (t >>> 8);
                t = 1468629610;buf[30] = (byte) (t >>> 1);
                t = 1707176958;buf[31] = (byte) (t >>> 24);
                t = -864251627;buf[32] = (byte) (t >>> 22);
                t = -1251796821;buf[33] = (byte) (t >>> 6);
                t = -1283499877;buf[34] = (byte) (t >>> 6);
                t = 2117376100;buf[35] = (byte) (t >>> 7);
                t = -1548979100;buf[36] = (byte) (t >>> 1);
                t = -797740266;buf[37] = (byte) (t >>> 3);
                t = 845614208;buf[38] = (byte) (t >>> 17);
                t = 1509344232;buf[39] = (byte) (t >>> 9);
                t = 428206908;buf[40] = (byte) (t >>> 18);
                t = -162798756;buf[41] = (byte) (t >>> 4);
                t = 1887931445;buf[42] = (byte) (t >>> 6);
                t = 213721140;buf[43] = (byte) (t >>> 22);
                t = 1934405538;buf[44] = (byte) (t >>> 20);
                t = 1660884272;buf[45] = (byte) (t >>> 24);
                t = 59029403;buf[46] = (byte) (t >>> 20);
                t = -1664501003;buf[47] = (byte) (t >>> 11);
                t = -1975726921;buf[48] = (byte) (t >>> 10);
                t = 461592777;buf[49] = (byte) (t >>> 6);
                t = -1029239947;buf[50] = (byte) (t >>> 13);
                t = -1181211075;buf[51] = (byte) (t >>> 4);
                t = -1607913041;buf[52] = (byte) (t >>> 3);
                t = 442519834;buf[53] = (byte) (t >>> 17);
                t = 1237862124;buf[54] = (byte) (t >>> 19);
                t = -318999648;buf[55] = (byte) (t >>> 9);
                t = -1911712474;buf[56] = (byte) (t >>> 14);
                t = -435601206;buf[57] = (byte) (t >>> 21);
                t = 282687372;buf[58] = (byte) (t >>> 18);
                t = -1773995209;buf[59] = (byte) (t >>> 3);
                t = 636266936;buf[60] = (byte) (t >>> 13);
                t = -1067159226;buf[61] = (byte) (t >>> 16);
                t = -119333820;buf[62] = (byte) (t >>> 11);
                t = -637116256;buf[63] = (byte) (t >>> 13);
                return new String(buf);}}.toString());
    }
}
