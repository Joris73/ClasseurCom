from PIL import Image
from PIL import ImageDraw
from PIL import ImageFont
from glob import glob
import os

inext = ".jpeg"
outext = ".ppm"

removeOld = False

def convert(path) :
	if "." in path : return
	os.chdir(path)
	files = glob("*"+inext)
	folders = list(set(glob("*")) - set(files))
	for i in folders :
		convert(path+'\\'+i)
	for i in files :
		img = Image.open(path+'\\'+i)
		img.save(path+"\\"+i[:-len(inext)]+outext)
		if removeOld : os.remove(path+"\\"+i)

convert(os.getcwd())