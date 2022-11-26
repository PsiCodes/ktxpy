#author=@PsiCodes
def checkWin(GigaState):
          if GigaState[0]==GigaState[3]==GigaState[6] !=0 or GigaState[0]==GigaState[1]==GigaState[2] !=0 or GigaState[2]==GigaState[5]==GigaState[8] !=0 or GigaState[1]==GigaState[4]==GigaState[7] !=0 or GigaState[0]==GigaState[4]==GigaState[8] !=0 or GigaState[2]==GigaState[4]==GigaState[6] !=0 or GigaState[0]==GigaState[4]==GigaState[5] !=0 or GigaState[6]==GigaState[7]==GigaState[8] !=0:
                    print("You won")
                    return False
          else:
                    return True

def setAndprint(GigaState,Index,Chance):
    if Chance==1:
       if(GigaState[Index]==0):
                      GigaState[Index]="X" 
       else :
                     GigaState[Index]
                     print("Please Enter The Index again Error")
                     setAndprint(GigaState,int(input("Enter correct index")), Chance)
    else:
               if(GigaState[Index]==0):
                      GigaState[Index]="Y" 
               else :
                      GigaState[Index]
                      print("Please Enter The Index again Error")
    print(f"{GigaState[0]} || {GigaState[1]} || {GigaState[2]} ")
    print(f"-------------")
    print(f"-------------")
    print(f"{GigaState[3]} || {GigaState[4]} || {GigaState[5]} ")
    print(f"-------------")
    print(f"-------------")
    print(f"{GigaState[6]} || {GigaState[7]} || {GigaState[8]} ")
    print(f"                                                              ")
    return GigaState,checkWin(GigaState)

if __name__=="__main__":
     print("Welcome TO TIC TAC TOE")
     GigaState=[0,0,0,0,0,0,0,0,0]
     Chance=1
     breakpoint=True
     while breakpoint:
             if Chance == 1:
                   print(f"Chance of X")
                   Xinput=int(input("Enter your Desired Index : "))
                   if Xinput<0 or Xinput>8:
                          print("Please Enter a Index between 0 and 8")
                          Xinput=int(input("Enter your Desired Index : "))
                          GigaState,breakpoint=setAndprint(GigaState, Xinput, Chance)
                   else:
                          GigaState,breakpoint=setAndprint(GigaState, Xinput, Chance)
                          
                   Chance=0
             else:
                   print(f"Chance of Y")
                   Yinput=int(input("Enter your Desired Index : "))
                   if Yinput<0 or Yinput>8:
                          print("Please Enter a Index between 0 and 8")
                          Yinput=int(input("Enter your Desired Index : "))
                          GigaState,breakpoint=setAndprint(GigaState, Yinput, Chance)
                   else:
                          GigaState,breakpoint=setAndprint(GigaState, Yinput, Chance)
                   Chance=1
             print(GigaState)
'''
0  1  2
3  4  5
6  7  8
'''