from PIL import Image
from PIL import ImageDraw
from PIL import ImageFont
from random import shuffle
import glob, os


#sizes = [(288,288),(432,432),(576,576)]
writetext = False
sizes = [(500,500)]
textcolor = ['purple','green','yellow','red','blue']
font = glob.glob("*.ttf")[0]

def fontSize(size) :
    if size <= 144 :
        return 36
    if size <= 288 :
        return 54
    if size > 288 :
        return 81
    


def specialCharRemover(string, byNothing) :
    while("_" in string):
        if byNothing :
            string = string.replace("_","")
        else :
            string = string.replace("_"," ")
    while("-" in string) :
        if byNothing :
            string = string.replace("-","")
        else :
            string = string.replace("-","\'")
    return string


def resize_and_crop(img_path, modified_path, size, text, crop_type='top'):
    global textcolor
    # If height is higher we resize vertically, if not we resize horizontally
    img = Image.open(img_path)
    # Get current and desired ratio for the images
    img_ratio = img.size[0] / float(img.size[1])
    ratio = size[0] / float(size[1])
    #The image is scaled/cropped vertically or horizontally depending on the ratio
    if ratio > img_ratio:
        img = img.resize((size[0], round(size[0] * img.size[1] / img.size[0])),
                Image.ANTIALIAS)
        # Crop in the top, middle or bottom
        if crop_type == 'top':
            box = (0, 0, img.size[0], size[1])
        elif crop_type == 'middle':
            box = (0, round((img.size[1] - size[1]) / 2), img.size[0],
                   round((img.size[1] + size[1]) / 2))
        elif crop_type == 'bottom':
            box = (0, img.size[1] - size[1], img.size[0], img.size[1])
        else :
            raise ValueError('ERROR: invalid value for crop_type')
        img = img.crop(box)
        
    elif ratio < img_ratio:
        img = img.resize((round(size[1] * img.size[0] / img.size[1]), size[1]),
                Image.ANTIALIAS)
        # Crop in the top, middle or bottom
        if crop_type == 'top':
            box = (0, 0, size[0], img.size[1])
        elif crop_type == 'middle':
            box = (round((img.size[0] - size[0]) / 2), 0,
                   round((img.size[0] + size[0]) / 2), img.size[1])
        elif crop_type == 'bottom':
            box = (img.size[0] - size[0], 0, img.size[0], img.size[1])
        else :
            raise ValueError('ERROR: invalid value for crop_type')
        img = img.crop(box)
    else :
        img = img.resize((size[0], size[1]),
                Image.ANTIALIAS)
        # If the scale is the same, we do not need to crop

    if writetext :
        msg = specialCharRemover(text, False)
        if (msg[0] == " ") :
            msg = msg[1:]
        msg = msg[0].upper() + msg[1:]
        
        draw = ImageDraw.Draw(img)
        fontsize = 200
        tfont = ImageFont.truetype(font, fontsize)
        w, h = draw.textsize(msg, tfont)
        while (size[0]-8 < w) :
            fontsize = int(fontsize * 0.9)
            tfont = ImageFont.truetype(font, fontsize)
            w, h = draw.textsize(msg, tfont)
        if('j' in msg) or ('g' in msg) or ('p' in msg) or ('y' in msg) :
            s = int(size[1]-(1.05*(fontsize)))
        else :
            s = int(size[1]-(0.77*(fontsize)))
        textcolor.append(textcolor[0])
        textcolor = textcolor[1:]
        draw.text((4, s), msg, textcolor[0], font=tfont)

    img.save(modified_path)
    

for size in sizes :
    if not(str(size) in os.listdir()) :
        os.mkdir(str(size))

nbfiles = len(glob.glob("*.jpg"))*len(sizes)
eff = 0
prceff = 0
for infile in glob.glob("*.jpg"):
    for size in sizes :
        file, ext = os.path.splitext(infile)
        #im = Image.open(infile)
        outfile = str(size)+"/"+infile[:-4]+".png"
        resize_and_crop(infile, outfile, size, infile[:-4], 'middle')
        eff+=1
        tempeff = int((eff*100)/nbfiles)
        if tempeff != prceff :
            prceff = tempeff
            print("Effectué : "+str(prceff)+"%")
        
