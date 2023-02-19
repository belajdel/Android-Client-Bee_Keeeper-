import cv2
import numpy as np
import RPi.GPIO as GPIO
from scipy import ndimage

# Set up the GPIO pin for the pump
GPIO.setmode(GPIO.BCM)
GPIO.setup(18, GPIO.OUT)

# create a video capture object using the index of your camera
cap = cv2.VideoCapture(0)

# check if camera is opened
if not cap.isOpened():
    print("Could not open camera")
    exit()

def conversion(image):
    gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    cv2.imwrite('gray_image.png',gray_image)
    cv2.imshow('color_image',image)
    cv2.imshow('gray_image',gray_image) 

def gaussian(image):
    cv2.getGaussianKernel(9,9)
    blur= cv2.GaussianBlur(image,(5,5),0)
    cv2.imwrite('blur.png',blur)
    cv2.imshow('blur',blur)

def averagefilter(image):
    kernel=np.ones((5,5),np.float32)/25
    dst= cv2.filter2D(image,-1,kernel)
    plt.subplot(121),plt.imshow(image),plt.title('blur')
    plt.xticks([]), plt.yticks([])
    plt.subplot(122),plt.imshow(dst),plt.title('averaged')
    plt.xticks([]), plt.yticks([])
    plt.show()
    cv2.imwrite('averaged.png',dst)
    
def segmentation():
    image = cv2.imread('averaged.png')
    gray = cv2.cvtColor(image,cv2.COLOR_BGR2GRAY)
   
    # Threshold the image using Otsu's method
    ret, thresh = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
 
    # Subtract the background from the original image
    bg = cv2.dilate(thresh, np.ones((3, 3), np.uint8), iterations=3)
    diff = cv2.absdiff(bg, thresh)
 
    # Perform morphological opening to remove small objects
    kernel = np.ones((3, 3), np.uint8)
    opening = cv2.morphologyEx(diff, cv2.MORPH_OPEN, kernel, iterations=2)
 
    # Count the number of pests in the image
    labelarray, particle_count = ndimage.measurements.label(opening)
 
    # Display the results
    cv2.imshow('Original', image)
    cv2.imshow('Subtracted', diff)
    cv2.imshow('Opening', opening)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
 
    print("Number of pests in the image:", particle_count)
    def send_to_database(particle_count, mil):
    sql = "INSERT INTO sessions (num_insects, mil) VALUES (%s, %s)"
    val = (particle_count, mil)
    mycursor.execute(sql, val)
    mydb.commit()
    print(mycursor.rowcount, "record inserted.")

    
    # Define the maximum number of pests allowed
    max_pests = 13
    
    # If the number of pests exceeds the maximum, trigger the pump
    if particle_count > max_pests:
        GPIO.output(18, GPIO.HIGH)  # Turn the pump on
    else:
        GPIO.output(18, GPIO.LOW)  # Turn the pump off


while True:
    # capture frame-by-frame
    ret, frame = cap.read()

    # if frame is read correctly, perform image processing and show results
    if ret:
        conversion(frame)
        gaussian(frame)
        averagefilter(frame)
        segmentation()

    # exit if 'q' is pressed
    if cv2.waitKey(1) == ord('q'):
        break

# release the capture and clean up
cap.release()
cv2.destroyAllWindows()
GPIO.cleanup()

