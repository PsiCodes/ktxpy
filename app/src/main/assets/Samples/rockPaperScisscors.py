#Author=@DSCSIT2020
import random

#game rule
print("Game rule:\n\tRock vs Paper => Paper Win \n\tPaper vs Scissor => Scissor Win \n\tRock vs Scissor => Rock Win")

#For play again
while True:  
    print("\nChoices are: \n\t1.Rock \n\t2.paper \n\t3.Scissor")

    #Computer choice    
    print("\nComputer's turn")
    print("----------------------")
    print("Computer has done secretly. Obviously You're not able to see it now")
    computer_choice = random.randint(1,3)
    if computer_choice == 1:
        computer_choice_name = "Rock"
    elif computer_choice == 2:
        computer_choice_name = "Paper"
    elif computer_choice == 3:
        computer_choice_name = "Scissor"

    # Print user choice
    print("\nYour turn")
    print("--------------")
    user_choice = int(input("Enter your choice: "))
    while user_choice > 3 or user_choice < 1: 
        user_choice = int(input("enter valid input: ")) 
    if user_choice == 1:
        user_choice_name = "Rock"
    elif user_choice == 2:
        user_choice_name = "Paper"
    else:
        user_choice_name = "Scissor"


    print("\n" + computer_choice_name + " vs " + user_choice_name)

    if computer_choice_name == user_choice_name:
        print("\nDraw")
    elif ((computer_choice_name == 1 and user_choice_name == 2) or (computer_choice_name == 2 and user_choice_name == 3) or (computer_choice_name == 3 and user_choice_name == 1)):
        print(user_choice_name + " wins. You Win")
    else:
        print(computer_choice_name + " wins. Computer win")

        print("\nDo you want to continue (Y/N)")
        answer=input()

        if (answer == "N" or answer == "n"):
            break

print("Thanks for playing")