import os
import shutil

ROOT = "C:/Users/Tim/Desktop/Photo/Icones"
OUTPUT = "C:/Users/Tim/Desktop/Photo/_release"
COPY = True

os.chdir(ROOT)
if os.path.isdir(OUTPUT) :
    shutil.rmtree(OUTPUT)
os.mkdir(OUTPUT)
os.mkdir(OUTPUT+"/img")
os.mkdir(OUTPUT+"/img/(500, 500)")
#listDir = os.listdir()
categorie = ""


def specialCharRemover(string, byNothing) :
    while("_" in string):
        if byNothing :
            if string[0] == "_" :
                string = string[1:]
            string = string.split("_")
            for i in range(len(string)) :
                string[i] = string[i][0].upper() + string[i][1:]
            string = "".join(string)
            string = string.split("'")
            for i in range(len(string)) :
                string[i] = string[i][0].upper() + string[i][1:]
            string = "".join(string)
        else :
            while "_" in string :
                string = string.replace("_"," ")
    while("-" in string) :
        if byNothing :
            while "-" in string :
                string = string.replace("-","")
        else :
            while "-" in string :
                string = string.replace("-","\'")
    if " " in string :
        if byNothing :
            string = string.split(" ")
            for i in range(len(string)) :
                string[i] = string[i][0].upper() + string[i][1:]
            string = "".join(string)
            
    return string[0].upper() + string[1:]

def folderParse(folder, copySize) :
    global categorie
    
    string = ""
    os.chdir(ROOT+folder+"/"+copySize)
    listStuff = os.listdir()
    listDir = []
    listFiles = []

    if ('(500, 500)' in listStuff):
        string += folderParse(folder, "(500, 500)")
        for i in listStuff :
            if not('(500, 500)' in i) :
                listDir.append(i)
    else :
        for i in listStuff :
            if ("." in i) :
                if not "desktop" in i :
                    listFiles.append(i)
            else :
                listDir.append(i)
                
    for i in listDir :
        if not("release" in i) :
            nameCategorie = specialCharRemover(i, False)
            nameImage = specialCharRemover(i, True)
            nameImage = nameImage.replace("'", "")
            if string != "" :
                string += ","
            string += "{\n"
            string += "\"Name\": \""+nameCategorie+"\",\n"
            string += "\"Image\": \"Categorie-"+nameImage+".png\",\n"
            string += "\"Items\": [\n"
            string += folderParse(folder+"/"+i,"")
            string += "]\n"
            string += "}\n"

    for i in listFiles :
        if copySize == "(500, 500)" :
            if (i[0] == "_") or (i[1] == "_") :
                filename = i
                while ("-" in filename) :
                    filename = filename.replace("-","")
                nameCategorie = specialCharRemover(i[1:-5], True)
                nameCategorie = nameCategorie[0].upper() + nameCategorie[1:]

    if 'nameCategorie' in locals():
        for i in listFiles :
            filename = specialCharRemover(i,True)
            
            if COPY :
                    if (i[0] == "_") or (i[1] == "_") :
                        print("Copying file : "+shutil.copy2(ROOT+folder+"/"+copySize+"/"+i,OUTPUT+"/img/"+copySize+"/Categorie-"+filename))
                    else :
                        print("Copying file : "+shutil.copy2(ROOT+folder+"/"+copySize+"/"+i,OUTPUT+"/img/"+copySize+"/Objet-"+nameCategorie+"."+filename))
            if copySize == "(500, 500)" :
                if not((i[0] == "_") or (i[1] == "_")) :
                    name = specialCharRemover(i[:-4], False)
                    name = name[0].upper() + name[1:]
                    filename = specialCharRemover(name, True)
                    if string != "" :
                        string += ","
                    string += "{\n"
                    string += "\"Name\": \""+name+"\",\n"
                    string += "\"Image\": \"Objet-"+nameCategorie+"."+filename+".png\"\n"
                    string += "}\n"
        
    return string
        
string = folderParse("", "")
string = "{\"ClasseurCom\": [\n" + string + "]}"

f = open(OUTPUT+"/code.json","w")
f.write(string)
f.close()

print("All done.")
