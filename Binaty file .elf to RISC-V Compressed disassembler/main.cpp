#include <iostream>
#include <string>
#include <stdio.h>
#include <map>
#include <unordered_map>
#include <vector>
#define ll long long

using namespace std;

const ll MAXN = 10e7;
unsigned char byteArr[MAXN];
ll addr, shnum, strPos, shrtPos, symPos, symSize;

ll get4Byte (ll beg) {
    return byteArr[beg] + 256 * (byteArr[beg + 1] + 256 * (byteArr[beg + 2] + 256 * byteArr[beg + 3]));
}

ll get2Byte (ll beg) {
    return byteArr[beg] + 256 * byteArr[beg + 1];
}

unordered_map<ll, string> labels;
unordered_map<ll, string> jumps;
ll cntJump = 0;

string getName (ll beg) {
    string res = "";
    for (ll i = 0; i < MAXN; i++) {
        if (byteArr[beg + i] == 0) {
            break;
        }
        res.push_back(byteArr[beg + i]);
    }
    return res;
}

map <ll, string> registers = {
        {0, "zero"}, {1, "ra"}, {2, "sp"}, {3, "gp"}, {4, "tp"}, {5, "t0"}, {6, "t1"}, {7, "t2"},
        {8, "s0"}, {9, "s1"}, {10, "a0"}, {11, "a1"}, {12, "a2"}, {13, "a3"}, {14, "a4"}, {15, "a5"},
        {16, "a6"}, {17, "a7"}, {18, "s2"}, {19, "s3"}, {20, "s4"}, {21, "s5"}, {22, "s6"}, {23, "s7"},
        {24, "s8"}, {25, "s9"}, {26, "s10"}, {27, "s11"}, {28, "t3"}, {29, "t4"}, {30, "t5"}, {31, "t6"}
};

map <ll, string> biopD = {
        {8, "mul"}, {9, "mulh"}, {10,"mulhsu"}, {11,"mulhu"}, {12,"div"}, {13,"divu"}, {14,"rem"}, {15,"remu"},
        {0, "add"}, {1, "sll"}, {2, "slt"}, {3, "sltu"}, {4, "xor"}, {5, "srl"}, {6, "or"}, {7, "and"},
        {256, "sub"}, {261, "sra"}
};

map <ll, string> immBiopD = {
        {0, "addi"}, {2, "slti"}, {3, "sltiu"}, {4, "xori"}, {6, "ori"}, {7, "andi"}, {261 /*0100000101*/, "srai"},
        {5, "srli"}, {1, "slli"}
};

map <ll, string> condopD = {
        {0, "beq"}, {1, "bne"}, {4, "blt"}, {5, "bge"}, {6, "bltu"}, {7, "bgeu"}
};


map <ll, string> loadopD = {
        {0, "lb"}, {1, "lh"}, {2, "lw"}, {4, "lbu"}, {5, "lhu"}
};

map <ll, string> storeopD = {
        {0, "sb"}, {1, "sh"}, {2, "sw"}
};

ll subbyte(ll a, ll beg, ll nd) {
    ll tmp = 0;
    for (ll i = 0; i <= beg; i++) {
        tmp <<= 1;
        tmp += 1;
    }
    a &= tmp;
    a = a >> nd;
    return a;
}

ll reverse(ll a, ll n) {
    ll tmp = 0;
    for (ll i = 0; i < n; i++) {
        tmp <<= 1;
        tmp += ((a >> i) & 1);
    }
    return tmp;
}

string storeop (ll cmd) {
    ll funct3 = subbyte(cmd, 14, 12);
    string rs2 = registers[subbyte(cmd, 24, 20)];
    string rs1 = registers[subbyte(cmd, 19, 15)];
    ll offset = (subbyte(cmd, 31, 25) << 5) + subbyte(cmd, 11, 7);
    offset = offset - (subbyte(cmd, 31, 31) << 12);
    return storeopD[funct3] + " " + rs2 + ", " + to_string(offset) + "(" + rs1 + ")";
}

string loadop (ll cmd) {
    ll funct3 = subbyte(cmd, 14, 12);
    string rs1 = registers[subbyte(cmd, 19, 15)];
    string rd = registers[subbyte(cmd, 11, 7)];
    ll offset = subbyte(cmd, 31, 20);
    offset = offset - (subbyte(cmd, 31, 31) << 12);
    return loadopD[funct3] + " " + rd + ", " + to_string(offset) + "(" + rs1 + ")";
}

string condop (ll cmd, ll pos) {
    ll funct3 = subbyte(cmd, 14, 12);
    string rs2 = registers[subbyte(cmd, 24, 20)];
    string rs1 = registers[subbyte(cmd, 19, 15)];
    ll offset = (subbyte(cmd, 31, 25) << 5) + subbyte(cmd, 11, 7);
    offset = (subbyte(cmd, 31, 31) << 11) + (subbyte(cmd, 7, 7) << 10) + (subbyte(cmd, 30, 25) << 4)
            + subbyte(cmd, 11, 8);
    offset *= 2;
    offset = offset - (subbyte(cmd, 31, 31) << 13);
    string lab = jumps[offset + pos];
    return condopD[funct3] + " " + rs1 + ", " + rs2 + ", " + lab;
}

string immBiop (ll cmd) {
    ll funct3 = subbyte(cmd, 14, 12);
    string rs1 = registers[subbyte(cmd, 19, 15)];
    string rd = registers[subbyte(cmd, 11, 7)];
    if (funct3 == 5 || funct3 == 1) {
        ll shamt = subbyte(cmd, 24, 20);
        return immBiopD[(subbyte(cmd, 31, 25) << 3) + funct3] + " " + rd + ", " + rs1 + ", " + to_string(shamt);
    } else {
        ll imm = subbyte(cmd, 31, 20);
        imm = imm - (subbyte(cmd, 31, 31) << 12);
        return immBiopD[funct3] + " " + rd + ", " + rs1 + ", " + to_string(imm);
    }
}

string biop (ll cmd) {
    ll funct73 = (subbyte(cmd, 31, 25) << 3) + subbyte(cmd, 14, 12);
    string op = biopD[funct73];
    string rs2 = registers[subbyte(cmd, 24, 20)];
    string rs1 = registers[subbyte(cmd, 19, 15)];
    string rd = registers[subbyte(cmd, 11, 7)];
    return op + " " + rd + "," + rs1 + "," + rs2;
}

map <ll, string> regC {
        {0, " s0"}, {1, "s1"}, {2, "a0"}, {3, "a1"}, {4, "a2"}, {5, "a3"}, {6 ,"a4"}, {7 ,"a5"}
};

map <ll, string> memC {
        {1, "c.fld"}, {2, "c.lw"}, {3, "c.flw"}, {5, "c.fsd"}, {6, "c.sw"}, {7, "c.fsw"}
};

map <ll, string> aropC {
        {0, "c.sub"}, {1, "c.xor"}, {2, "c.or"}, {3, "c.and"}, {4, "c.subw"}, {5, "c.addw"}
};

string hexnums = "0123456789abcdef";

void jump(ll offset, ll pos) {
    if (labels.find(offset + pos) == labels.end()) {
        string str = "LOC_";
        string hex = "";
        ll tmp = cntJump;
        while (tmp > 0) {
            hex = hexnums[tmp % 16] + hex;
            tmp /= 16;
        }
        for (ll i = 0; i < 5 - hex.length(); i++) {
            str.push_back('0');
        }
        str = str + hex;
        jumps[offset + pos] = str;
        labels[pos + offset] = str;
        //cout << pos << " " << cntJump << " " << str << " " << to_string(offset) << endl;
        cntJump++;
    } else {
        jumps[offset + pos] = labels[offset + pos];
    }
}

void cmd2byteJumps (ll cmd, ll pos) {
    ll opcode = subbyte(cmd, 1, 0);
    ll funct3 = subbyte(cmd, 15, 13);
    switch (opcode) {
        case 1: //01
            if (funct3 == 1) {
                ll offset = (subbyte(cmd, 12, 12) << 10) + (subbyte(cmd, 8, 8) << 9) + (subbyte(cmd, 10, 10) << 8) +
                             (subbyte(cmd, 9, 9) << 7) + (subbyte(cmd, 6, 6) << 6) + (subbyte(cmd, 7, 7) << 5) +
                             (subbyte(cmd, 2, 2) << 4) + (subbyte(cmd, 11, 11) << 3) + subbyte(cmd, 5, 3);
                offset *= 2;
                offset = offset - (subbyte(cmd, 12, 12) << 12);
                jump(offset, pos);
                //return "c.jal " + jumps[offset];
            } else if (funct3 == 5) {
                ll offset = (subbyte(cmd, 12, 12) << 10) + (subbyte(cmd, 8, 8) << 9) + (subbyte(cmd, 10, 10) << 8) +
                             (subbyte(cmd, 9, 9) << 7) + (subbyte(cmd, 6, 6) << 6) + (subbyte(cmd, 7, 7) << 5) +
                             (subbyte(cmd, 2, 2) << 4) + (subbyte(cmd, 11, 11) << 3) + subbyte(cmd, 5, 3);
                offset *= 2;
                offset = offset - (subbyte(cmd, 12, 12) << 12);
                jump(offset, pos);
                //return "c.j " + jumps[offset];
            } else if (funct3 == 6) {
                ll imm = (subbyte(cmd, 12, 12) << 7) + (subbyte(cmd, 6, 6) << 6) + (subbyte(cmd, 5, 5) << 5) +
                         (subbyte(cmd, 2, 2) << 4) + (subbyte(cmd, 11, 10) << 2) + subbyte(cmd, 4, 3);
                imm *= 2;
                imm = imm - (subbyte(cmd, 12, 12) << 9);
                jump(imm, pos);
                //return "c.beqz " + jumps[imm];
            } else if (funct3 == 7) {
                ll imm = (subbyte(cmd, 12, 12) << 7) + (subbyte(cmd, 6, 6) << 6) + (subbyte(cmd, 5, 5) << 5) +
                         (subbyte(cmd, 2, 2) << 4) + (subbyte(cmd, 11, 10) << 2) + subbyte(cmd, 4, 3);
                imm *= 2;
                imm = imm - (subbyte(cmd, 12, 12) << 9);
                jump(imm, pos);
                //return "c.bnez " + jumps[imm];
            }
            break;
        default:
            return;
    }
    return;
}

string cmd2byte (ll cmd, ll pos) {
    ll opcode = subbyte(cmd, 1, 0);
    ll funct3 = subbyte(cmd, 15, 13);
    switch (opcode) {
        case 0: //00
            if (funct3  == 0) {
                ll nzuimm = subbyte(cmd, 12, 5);
                string rdc = regC[subbyte(cmd, 4, 2)];
                return "c.addi4spn " + rdc + ", sp, " + to_string(nzuimm);
            } else {
                ll uimm = (subbyte(cmd, 12, 10) << 2) + subbyte(cmd, 6, 5);
                string rs1c = regC[subbyte(cmd, 9, 7)];
                string rdc = regC[subbyte(cmd, 4, 2)];
                return memC[funct3] + " " + rdc + ", " + to_string(uimm) + "(" + rs1c + ")";
            }
            break;
        case 1: //01
            if (funct3 == 0) {
                ll nzimm = (subbyte(cmd, 12, 12) << 5) + subbyte(cmd, 6, 2);
                nzimm = nzimm - (subbyte(cmd, 12, 12) << 6);
                string rd = registers[subbyte(cmd, 11, 7)];
                if (rd == "zero") {
                    return "c.nop";
                } else {
                    return "c.addi " + rd + ", " + to_string(nzimm);
                }
            } else if (funct3 == 1) {
                ll offset = (subbyte(cmd, 12, 12) << 10) + (subbyte(cmd, 8, 8) << 9) + (subbyte(cmd, 10, 10) << 8) +
                        (subbyte(cmd, 9, 9) << 7) + (subbyte(cmd, 6, 6) << 6) + (subbyte(cmd, 7, 7) << 5) +
                        (subbyte(cmd, 2, 2) << 4) + (subbyte(cmd, 11, 11) << 3) + subbyte(cmd, 5, 3);
                offset *= 2;
                offset = offset - (subbyte(cmd, 12, 12) << 12);
                return "c.jal " + jumps[offset + pos];
            } else if (funct3 == 2) {
                ll imm = (subbyte(cmd, 12, 12) << 5) + subbyte(cmd, 6, 2);
                string rd = registers[subbyte(cmd, 11, 7)];
                return "c.li " + rd + ", " + to_string(imm);
            } else if (funct3 == 3) {
                string rd = registers[subbyte(cmd, 11, 7)];
                ll imm = (subbyte(cmd, 12, 12) << 5) + subbyte(cmd, 6, 2);
                imm = imm - (subbyte(cmd, 12, 12) << 6);
                if (rd == "sp") {
                    return "c.addi16sp sp, " + to_string(imm);
                } else {
                    return "c.lui " + rd + ", " + to_string(imm);
                }
            } else if (funct3 == 4) {
                ll funct6 = subbyte(cmd, 11, 10);
                string rdc = regC[subbyte(cmd, 9, 7)];
                if (funct6 < 3) { // 00 01 10
                    ll nzuimm = (subbyte(cmd, 12, 12) << 5) + subbyte(cmd, 6, 2);
                    if (funct6 == 0) { // 00
                        if (nzuimm != 0) {
                            return "c.srli " + rdc + ", " + to_string(nzuimm);
                        } else {
                            return "c.srli64 " + rdc;
                        }
                    } else if (funct6 == 1) { // 01
                        if (nzuimm != 0) {
                            return "c.srai " + rdc + ", " + to_string(nzuimm);
                        } else {
                            return "c.srai64 " + rdc;
                        }
                    } else { // 10
                        return "c.andi " + rdc + ", " + to_string(nzuimm);
                    }
                } else { // 11
                    ll funct12 = (subbyte(cmd, 12, 12) << 2) + subbyte(cmd, 6, 5);
                    string rs2c = regC[subbyte(cmd, 4, 2)];
                    return aropC[funct12] + " " + rdc + ", " + rs2c;
                }
            } else if (funct3 == 5) {
                ll offset = (subbyte(cmd, 12, 12) << 10) + (subbyte(cmd, 8, 8) << 9) + (subbyte(cmd, 10, 10) << 8) +
                             (subbyte(cmd, 9, 9) << 7) + (subbyte(cmd, 6, 6) << 6) + (subbyte(cmd, 7, 7) << 5) +
                             (subbyte(cmd, 2, 2) << 4) + (subbyte(cmd, 11, 11) << 3) + subbyte(cmd, 5, 3);
                offset *= 2;
                offset = offset - (subbyte(cmd, 12, 12) << 12);
                return "c.j " + jumps[offset + pos];
            } else if (funct3 == 6) {
                ll imm = (subbyte(cmd, 12, 12) << 7) + (subbyte(cmd, 6, 6) << 6) + (subbyte(cmd, 5, 5) << 5) +
                         (subbyte(cmd, 2, 2) << 4) + (subbyte(cmd, 11, 10) << 2) + subbyte(cmd, 4, 3);
                imm *= 2;
                imm = imm - (subbyte(cmd, 12, 12) << 9);
                string rs1c = regC[subbyte(cmd, 9, 7)];
                return "c.beqz " + jumps[imm + pos];
            } else if (funct3 == 7) {
                ll imm = (subbyte(cmd, 12, 12) << 7) + (subbyte(cmd, 6, 6) << 6) + (subbyte(cmd, 5, 5) << 5) +
                        (subbyte(cmd, 2, 2) << 4) + (subbyte(cmd, 11, 10) << 2) + subbyte(cmd, 4, 3);
                imm *= 2;
                imm = imm - (subbyte(cmd, 12, 12) << 9);
                string rs1c = regC[subbyte(cmd, 9, 7)];
                return "c.bnez " + jumps[imm + pos];
            }
            break;
        case 2: //10
            if (funct3 == 0) { // 000
                ll nzuimm = (subbyte(cmd, 12, 12) << 5) + subbyte(cmd, 6, 2);
                string rd = registers[subbyte(cmd, 11, 7)];
                if (nzuimm != 0) {
                    return "c.slli " + rd + ", " + to_string(nzuimm);
                } else {
                    return "c.slli64 " + rd;
                }
            } else if (funct3 == 2) { // 010
                ll nzuimm = (subbyte(cmd, 12, 12) << 5) + subbyte(cmd, 6, 2);
                string rd = registers[subbyte(cmd, 11, 7)];
                return "c.lwsp " + rd + ", " + to_string(nzuimm) + "(sp)";
            } else if (funct3 == 3) { // 011
                ll nzuimm = (subbyte(cmd, 12, 12) << 5) + subbyte(cmd, 6, 2);
                string rd = registers[subbyte(cmd, 11, 7)];
                return "c.ldsp " + rd + ", " + to_string(nzuimm) + "(sp)";
            } else if (funct3 == 4) { // 100
                string rs1 = registers[subbyte(cmd, 11, 7)];
                string rs2 = registers[subbyte(cmd, 6, 2)];
                ll funct1 = subbyte(cmd, 12, 12);
                if (funct1 == 0) { // 0
                    if (rs2 == "zero") {
                        return "c.jr " + rs1;
                    } else {
                        return "c.mv " + rs1 + ", " + rs2;
                    }
                } else { // 1
                    if (rs1 == "zero") { // 0 0
                        return "c.ebreak";
                    } else if (rs2 == "zero") { // rs1 0

                        return "c.jalr " + rs1;
                    } else { // rs1 rs2
                        return  "c.add" + rs1 + ", " + rs2;
                    }
                }
            } else if (funct3 == 5) {
                string rs2 = registers[subbyte(cmd, 6, 2)];
                ll uimm = subbyte(cmd, 12, 7);
                return "c.fsdsp " + rs2 + ", " + to_string(uimm) + "(sp)";
            } else if (funct3 == 6) {
                string rs2 = registers[subbyte(cmd, 6, 2)];
                ll uimm = subbyte(cmd, 12, 7);
                return "c.swsp " + rs2 + ", " + to_string(uimm) + "(sp)";
            } else if (funct3 == 7) {
                string rs2 = registers[subbyte(cmd, 6, 2)];
                ll uimm = subbyte(cmd, 12, 7);
                return "c.fswsp " + rs2 + ", " + to_string(uimm) + "(sp)";
            }
        default:
            return "unknown_command";
    }
}

string cmd4byte (ll cmd, ll pos) {
    ll opcode = subbyte(cmd, 6, 2);
    string rd = registers[subbyte(cmd, 11, 7)];
    ll imm = subbyte(cmd, 31, 12);
    string lab;
    switch (opcode) {
        case 12: //01100
            return biop(cmd);
            break;
        case 4: //00100
            return immBiop(cmd);
            break;
        case 24: //11000
            return condop(cmd, pos);
            break;
        case 0: //0000
            return loadop(cmd);
            break;
        case 8: //1000
            return storeop(cmd);
            break;
        case 13: //01101
            return "lui " + rd + ", " + to_string(imm);
            break;
        case 5: //00101
            return "auipc " + rd + ", " + to_string(imm);
            break;
        case 27: //11011
            imm = (subbyte(cmd, 31, 31) << 20) + (subbyte(cmd, 19, 12) << 12) + (subbyte(cmd, 20, 20) << 11)
                    + (subbyte(cmd, 30, 21) << 1);
            imm = imm - (subbyte(cmd, 31, 31) << 21);
            lab = jumps[imm + pos];
            return "jal " + rd + ", " + lab;
            break;
        case 25: //11001
            imm = 2 * subbyte(cmd, 31, 20);
            imm = imm - (subbyte(cmd, 31, 31) << 13);
            lab = jumps[imm + pos];
            return "jalr " + rd + ", " + registers[subbyte(cmd, 19, 15)] + ", " + lab;
            break;
        case 28: //11100
            if (subbyte(cmd, 20, 20) == 0) {
                return "ecall";
            } else {
                return "ebreak";
            }
            break;
        default:
            return "unknown_command";
    }
}

void cmd4byteJumps (ll cmd, ll pos) {
    ll opcode = subbyte(cmd, 6, 2);
    string rd = registers[subbyte(cmd, 11, 7)];
    ll offset = (subbyte(cmd, 31, 25) << 5) + subbyte(cmd, 11, 7);
    ll imm = subbyte(cmd, 31, 12);
    switch (opcode) {
        case 24: //11000 Conditional jump
            offset = (subbyte(cmd, 31, 31) << 11) + (subbyte(cmd, 7, 7) << 10) + (subbyte(cmd, 30, 25) << 4)
                    + subbyte(cmd, 11, 8);
            offset *= 2;
            offset = offset - (subbyte(cmd, 31, 31) << 13);
            jump(offset, pos);
            break;
        case 27: //11011 // jal
            imm = (subbyte(cmd, 31, 31) << 20) + (subbyte(cmd, 19, 12) << 12) + (subbyte(cmd, 20, 20) << 11)
                    + (subbyte(cmd, 30, 21) << 1);
            imm = imm - (subbyte(cmd, 31, 31) << 21);
            jump(imm, pos);
            break;
        case 25: //11001 // jalr
            imm = 2 * subbyte(cmd, 31, 20);
            imm = imm - (subbyte(cmd, 31, 31) << 13);
            jump(imm, pos);
            break;;
        default:
            return;
    }
}

string getStr4Byte (ll beg) {
    ll a = get4Byte(beg);
    string ans = "";
    for (ll i = 31; i >= 0; i--) {
        ans += ((a >> i) & 1) + '0';
    }
    return ans;
}

int main(int argc, char *argv[])
{
    FILE *file;
    char *fname = argv[1];
    file = fopen(fname, "rb");
    char b;
    ll n;
    for (ll i = 0; fread(&byteArr[i],sizeof(byteArr[i]),1,file); i++);
    fclose(file);
    addr = get4Byte(32);
    shnum = get2Byte(48);
    for (ll i = 0; i < shnum; i++) { // seek sym and str
        ll type = get4Byte(addr + 4);
        if (type == 2) {
            symPos = get4Byte(addr + 16);
            symSize = get4Byte(addr + 20) / 16;
        }
        if (type == 3 && get2Byte(50) != i) {
            strPos = get4Byte(addr + 16);
        }
        if (type == 3 && get2Byte(50) == i) {
            shrtPos = get4Byte(addr + 16);
        }
        addr += 40;
    }
    addr = get4Byte(32);
    ll begtxt;
    for (ll i = 0; i < shnum; i++) { // seek .text
        if (getName(shrtPos + get4Byte(addr)) == ".text") {
            begtxt = get4Byte(addr + 16);
            n = begtxt + get4Byte(addr + 20);
            break;
        }
        addr += 40;
    }
    addr = symPos;
    vector<ll> value, sze;
    vector<string> strType, strBnd, strVis, strIndex, name;
    for (ll i = 0; i < symSize; i++) {
        value.push_back(get4Byte(addr + 4));
        sze.push_back(get4Byte(addr + 8));
        ll info = byteArr[addr + 12];
        ll bnd = (info) >> 4;
        ll type = (info) & 0xf;
        ll vis = byteArr[addr + 13] & 0x3;
        ll index = get2Byte(addr + 14);
        ll namePos = get4Byte(addr);
        name.push_back(getName(strPos + namePos));
        labels[value[i] - 65536] = name[i];
        switch (bnd) {
            case 0:
                strBnd.push_back("LOCAL");
                break;
            case 1:
                strBnd.push_back("GLOBAL");
                break;
            case 2:
                strBnd.push_back("WEAK");
                break;
            case 10:
                strBnd.push_back("LOOS");
                break;
            case 12:
                strBnd.push_back("HIOS");
                break;
            case 13:
                strBnd.push_back("LOPROC");
                break;
            case 15:
                strBnd.push_back("HIPROC");
                break;
        }
        switch (type) {
            case 0:
                strType.push_back("NOTYPE");
                break;
            case 1:
                strType.push_back("OBJECT");
                break;
            case 2:
                strType.push_back("FUNC");
                break;
            case 3:
                strType.push_back("SECTION");
                break;
            case 4:
                strType.push_back("FILE");
                break;
            case 5:
                strType.push_back("COMMON");
                break;
            case 6:
                strType.push_back("TLS");
                break;
            case 10:
                strType.push_back("LOOS");
                break;
            case 12:
                strType.push_back("HIOS");
                break;
            case 13:
                strType.push_back("LOPROC");
                break;
            case 14:
                strType.push_back("SPARC_REGISTER");
                break;
            case 15:
                strType.push_back("HIPROC");
                break;

        }
        switch (vis) {
            case 0:
                strVis.push_back("DEFAULT");
                break;
            case 1:
                strVis.push_back("llERNAL");
                break;
            case 2:
                strVis.push_back("HIDDEN");
                break;
            case 3:
                strVis.push_back("PROTECTED");
                break;
            case 4:
                strVis.push_back("EXPORTED");
                break;
            case 5:
                strVis.push_back("SINGLETON");
                break;
            case 6:
                strVis.push_back("ELIMINATE");
                break;
        }
        switch (index) {
            case 0:
                strIndex.push_back("UNDEF");
                break;
            case 0xff00:
                strIndex.push_back("LORESERVE");
                break;
            case 0xff01:
                strIndex.push_back("AFTER");
                break;
            case 0xff02:
                strIndex.push_back("AMD64_LCOMMON");
                break;
            case 0xff1f:
                strIndex.push_back("HIPROC");
                break;
            case 0xff20:
                strIndex.push_back("LOOS");
                break;
            case 0xff3f:
                strIndex.push_back("LOSUNW");
                break;
            case 0xfff1:
                strIndex.push_back("ABS");
                break;
            case 0xfff2:
                strIndex.push_back("COMMON");
                break;
            case 0xffff:
                strIndex.push_back("XINDEX");
                break;
            default:
                strIndex.push_back(to_string(index));
        }
        addr += 16;
    }
    ll cmd;
    for (ll addr = begtxt; addr < n;) {
        if (n - addr >= 4) {
            if (subbyte(get4Byte(addr), 1, 0) == 3 /*11*/) {
                cmd = get4Byte(addr);
                cmd4byteJumps(cmd, addr);
            } else {
                cmd = get2Byte(addr);
                cmd2byteJumps(cmd, addr);
                addr += 2;
                continue;
            }
        } else {
            cmd = get2Byte(addr);
            cmd2byteJumps(cmd, addr);
        }
        addr = n - addr >= 4 ? addr + 4 : addr + 2;
    }
    FILE *fout;
    char *fnameout = argv[2];
    fout = fopen(fnameout, "w");
    fprintf(fout, ".text\n");
    for (ll addr = begtxt; addr < n;) {
        string lab = "";
        string str;
        int outaddr = addr + 65536;
        if (n - addr >= 4) {
            if (subbyte(get4Byte(addr), 1, 0) == 3 /*11*/) {
                cmd = get4Byte(addr);
                str =  cmd4byte(cmd, addr);
                if (labels.find(addr) != labels.end()) {
                    lab = labels[addr] + ":";
                }
                fprintf(fout, "%08x %10s %s\n", outaddr, lab.c_str(), str.c_str());
            } else {
                cmd = get2Byte(addr);
                str = cmd2byte(cmd, addr);
                if (labels.find(addr) != labels.end()) {
                    lab = labels[addr] + ":";
                }
                fprintf(fout, "%08x %10s %s\n", outaddr, lab.c_str(), str.c_str());
                addr += 2;
                continue;
            }
        } else {
            ll cmd = get2Byte(addr);
            str = cmd2byte(cmd, addr);
            if (labels.find(addr) != labels.end()) {
                lab = labels[addr] + ":";
            }
            fprintf(fout, "%08x %10s %s\n", outaddr, lab.c_str(), str.c_str());
        }
        addr = n - addr >= 4 ? addr + 4 : addr + 2;
    }
    fprintf(fout, "\n.symtab\n");
    string s1 = "Symbol", s2 = "Value", s3 = "Size", s4 = "Type", s5 = "Bind", s6 = "Vis", s7 = "Index", s8 = "Name";
    fprintf(fout, "%s %-15s %7s %-8s %-8s %-8s %6s %s\n", s1.c_str(), s2.c_str(), s3.c_str(), s4.c_str(), s5.c_str(),
            s6.c_str(), s7.c_str(), s8.c_str());
    for (int i = 0; i < symSize; i++) {
        fprintf(fout, "[%4i] 0x%-15X %5i %-8s %-8s %-8s %6s %s\n", i, value[i], sze[i], strType[i].c_str(),
                strBnd[i].c_str(), strVis[i].c_str(), strIndex[i].c_str(), name[i].c_str());
    }
}