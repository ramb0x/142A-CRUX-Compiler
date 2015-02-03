.data                         # BEGIN Data Segment
cruxdata.canary_begin: .space 4
cruxdata.a: .space 12
cruxdata.canary_end: .space 4
data.newline:      .asciiz       "\n"
data.floatquery:   .asciiz       "float?"
data.intquery:     .asciiz       "int?"
data.trueString:   .asciiz       "true"
data.falseString:  .asciiz       "false"
                              # END Data Segment
.text                         # BEGIN Code Segment
func.printBool:
lw $a0, 0($sp)
beqz $a0, label.printBool.loadFalse
la $a0, data.trueString
j label.printBool.join
label.printBool.loadFalse:
la $a0, data.falseString
label.printBool.join:
li   $v0, 4
syscall
jr $ra
func.printFloat:
l.s  $f12, 0($sp)
li   $v0,  2
syscall
jr $ra
func.printInt:
lw   $a0, 0($sp)
li   $v0, 1
syscall
jr $ra
func.println:
la   $a0, data.newline
li   $v0, 4
syscall
jr $ra
func.readFloat:
la   $a0, data.floatquery
li   $v0, 4
syscall
li   $v0, 6
syscall
mfc1 $v0, $f0
jr $ra
func.readInt:
la   $a0, data.intquery
li   $v0, 4
syscall
li   $v0, 5
syscall
jr $ra
.text                         # BEGIN Crux Program
main:
subu $sp, $sp, 8
sw $fp, 0($sp)
sw $ra, 4($sp)
addi $fp, $sp, 8
subu $sp, $sp, 4
la $t0, cruxdata.canary_begin
subu $sp, $sp, 4
sw $t0, 0($sp)
li $t0, 0
subu $sp, $sp, 4
sw $t0, 0($sp)
la $t0, cruxdata.canary_end
subu $sp, $sp, 4
sw $t0, 0($sp)
li $t0, 0
subu $sp, $sp, 4
sw $t0, 0($sp)
la $t0, cruxdata.a
subu $sp, $sp, 4
sw $t0, 0($sp)
li $t0, 0
subu $sp, $sp, 4
sw $t0, 0($sp)
lw $t0, 0($sp)
addiu $sp, $sp, 4
lw $t1, 0($sp)
addiu $sp, $sp, 4
li $t2, 0
mul $t3, $t0, $t2
add $t4, $t1, $t3
subu $sp, $sp, 4
sw $t4, 0($sp)
li.s $f0, 1.11
subu $sp, $sp, 4
swc1 $f0, 0($sp)
la $t0, cruxdata.a
subu $sp, $sp, 4
sw $t0, 0($sp)
li $t0, 1
subu $sp, $sp, 4
sw $t0, 0($sp)
lw $t0, 0($sp)
addiu $sp, $sp, 4
lw $t1, 0($sp)
addiu $sp, $sp, 4
li $t2, 0
mul $t3, $t0, $t2
add $t4, $t1, $t3
subu $sp, $sp, 4
sw $t4, 0($sp)
li.s $f0, 2.22
subu $sp, $sp, 4
swc1 $f0, 0($sp)
la $t0, cruxdata.a
subu $sp, $sp, 4
sw $t0, 0($sp)
li $t0, 2
subu $sp, $sp, 4
sw $t0, 0($sp)
lw $t0, 0($sp)
addiu $sp, $sp, 4
lw $t1, 0($sp)
addiu $sp, $sp, 4
li $t2, 0
mul $t3, $t0, $t2
add $t4, $t1, $t3
subu $sp, $sp, 4
sw $t4, 0($sp)
li.s $f0, 3.33
subu $sp, $sp, 4
swc1 $f0, 0($sp)
la $t0, cruxdata.canary_begin
subu $sp, $sp, 4
sw $t0, 0($sp)
lw $t0, 0($sp)
addiu $sp, $sp, 4
lw $t1, 0($t0)
subu $sp, $sp, 4
sw $t1, 0($sp)
jal func.printInt
addi $sp, $sp, 4
jal func.println
addi $t0, $fp, -12
subu $sp, $sp, 4
sw $t0, 0($sp)
li $t0, 0
subu $sp, $sp, 4
sw $t0, 0($sp)
label.1:
addi $t0, $fp, -12
subu $sp, $sp, 4
sw $t0, 0($sp)
lw $t0, 0($sp)
addiu $sp, $sp, 4
lw $t1, 0($t0)
subu $sp, $sp, 4
sw $t1, 0($sp)
li $t0, 3
subu $sp, $sp, 4
sw $t0, 0($sp)
lw $t1, 0($sp)
addiu $sp, $sp, 4
lw $t0, 0($sp)
addiu $sp, $sp, 4
slt $t2, $t0, $t1
subu $sp, $sp, 4
sw $t2, 0($sp)
lw $t7, 0($sp)
addiu $sp, $sp, 4
beqz $t7, label.2
la $t0, cruxdata.a
subu $sp, $sp, 4
sw $t0, 0($sp)
addi $t0, $fp, -12
subu $sp, $sp, 4
sw $t0, 0($sp)
lw $t0, 0($sp)
addiu $sp, $sp, 4
lw $t1, 0($t0)
subu $sp, $sp, 4
sw $t1, 0($sp)
lw $t0, 0($sp)
addiu $sp, $sp, 4
lw $t1, 0($sp)
addiu $sp, $sp, 4
li $t2, 0
mul $t3, $t0, $t2
add $t4, $t1, $t3
subu $sp, $sp, 4
sw $t4, 0($sp)
lw $t0, 0($sp)
addiu $sp, $sp, 4
lwc1 $f0, 0($t0)
subu $sp, $sp, 4
swc1 $f0, 0($sp)
jal func.printFloat
addi $sp, $sp, 4
jal func.println
addi $t0, $fp, -12
subu $sp, $sp, 4
sw $t0, 0($sp)
addi $t0, $fp, -12
subu $sp, $sp, 4
sw $t0, 0($sp)
lw $t0, 0($sp)
addiu $sp, $sp, 4
lw $t1, 0($t0)
subu $sp, $sp, 4
sw $t1, 0($sp)
li $t0, 1
subu $sp, $sp, 4
sw $t0, 0($sp)
lw $t1, 0($sp)
addiu $sp, $sp, 4
lw $t0, 0($sp)
addiu $sp, $sp, 4
add $t2, $t0, $t1
subu $sp, $sp, 4
sw $t2, 0($sp)
b label.1
label.2:
la $t0, cruxdata.canary_end
subu $sp, $sp, 4
sw $t0, 0($sp)
lw $t0, 0($sp)
addiu $sp, $sp, 4
lw $t1, 0($t0)
subu $sp, $sp, 4
sw $t1, 0($sp)
jal func.printInt
addi $sp, $sp, 4
label.0:
addu $sp, $sp, 4
li $v0, 10
syscall
                              # END Code Segment
