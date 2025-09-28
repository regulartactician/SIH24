import streamlit as st
from firebase_admin import credentials, db
import firebase_admin

# Configure the page layout
st.set_page_config(layout="wide", initial_sidebar_state="collapsed")
st.write('<style>div.block-container{padding-top:0rem;}</style>', unsafe_allow_html=True)

# Apply custom styling
st.markdown("""
    <style>
    div.stButton > button:first-child {
        background-color: #003FDD;
        color: white;
        width: 200px;
        margin-top: 20px;
        text-align: center;
    }
    summary {
        background-color: #003FDD;
        font-size: 16px;
        font-weight: bold;
        border-radius: 20px;
        color: white;
        width: 200px;
        text-align: center;
    }
    .stVerticalBlockBorderWrapper {
        border: solid 2px #003FDD;
    }
    .st-emotion-cache-1puwf6r p {
        font-size: 18px;
        color: white;
    }
    </style>
""", unsafe_allow_html=True)
st.markdown('<hr style="border:solid 2px #003FDD;margin: 0em;">', unsafe_allow_html=True)


st.title("Hello, Admin!")


# Firebase initialization
cred_path = "jaldisihdb-firebase-adminsdk-m5jux-16bbbd3d4b.json"
try:
    if not firebase_admin._apps:
        cred = credentials.Certificate(cred_path)
        firebase_admin.initialize_app(cred, {
            'databaseURL': 'https://jaldisihdb-default-rtdb.firebaseio.com/'
        })
except Exception as e:
    st.error(f"Firebase initialization error: {e}")

# Layout for login inputs
with st.container(border=True):
    a, waste = st.columns([0.4, 0.6])
    with a:
        st.subheader("Login")
        uid = st.text_input("Enter Admin UserID: ")
        pw = st.text_input("Enter Password:", type="password")

        if st.button("Login"):
            try:
                # Fetch the Admin data from Firebase
                ref = db.reference("Admin").get()
                st.write("Admin Data:", ref)  # Debug Firebase response
                
                if ref is None:
                    st.warning("No Admin data found. Please sign up first.")
                elif uid in ref:
                    ref2 = db.reference(f"Admin/{uid}").get()
                    if ref2.get("pw") == pw:
                        st.success("Login Successful!")
                        st.session_state={}
                        st.session_state["uid"] = uid
                        st.switch_page("pages/admin_all.py")
                    else:
                        st.error("Incorrect Password!")
                else:
                    st.error("Incorrect Username!")
            except Exception as e:
                st.error(f"Error during login: {e}")

    # Footer or additional instructions
    st.markdown("""
        <div style="text-align: center; margin-top: 20px;">
            <p style="font-size: 14px; color: #555;">
                Please contact the administrator if you face any issues logging in.
            </p>
        </div>
    """, unsafe_allow_html=True)
