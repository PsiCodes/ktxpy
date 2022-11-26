import random
def numberGuesserGame():
    r:int=random.randint(0, 15)
    gameState=True
    while(gameState):
        a:int=int(input("Guess The Number Between 0 to 15 : "))
        if (r-a)<0:
            print("\ntry smaller number")
        elif(r-a)>0:
            print("\ntry bigger number")
        else:
            print("\nYou Won")
            gameState=False
if __name__=="__main__":
    numberGuesserGame()
