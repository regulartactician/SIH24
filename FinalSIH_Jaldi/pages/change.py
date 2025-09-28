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

with st.container(border=True,key="pass"):
    old=st.text_input("Old Password")
    new=st.text_input("New Password")
    confirm=st.text_input("Confirm New Password")
    st.write(st.session_state)

    if st.button("Change Password"):
        try:
            ref = db.reference("Admin").get()
            st.write("Admin Data:", ref)

            if ref is None:
                st.warning("No Admin data found. Please sign up first.")
            else:
                old1=st.session_state
                uid = old1["uid"]
                ref2 = db.reference(f"Admin/{uid}").get()

                if ref2.get("pw") == old:
                    if new == confirm:
                        db.reference(f"Admin/{uid}").set({"pw": new})
                        st.success("Password changed successfully!")
                        st.switch_page("admin_login.py")
                    else:
                        st.error("New password and confirmation password don't match!")
                else:
                    st.error("Incorrect old password!")

        except Exception as e:
            st.error(f"Error during password change: {e}")

    st.markdown("""
        <div style="text-align: center; margin-top: 20px;">
            <p style="font-size: 14px; color: #555;">
                Please contact the administrator if you face any issues logging in.
            </p>
        </div>
    """, unsafe_allow_html=True)