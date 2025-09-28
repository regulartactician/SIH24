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

# Title and separator
st.markdown('<hr style="border:solid 2px #003FDD;margin: 0em;">', unsafe_allow_html=True)

a,b,c = st.columns([0.7,0.15,0.15])
st.markdown('''<hr style="border:solid 2px #003FDD;margin: 0em;">''',unsafe_allow_html=True)
with a:
    st.title(f"Issue Readdresal Portal")
with b:
    if st.button("Home Page"):
        st.switch_page("pages/admin_all.py")
with c:
    if st.button("Logout"):
        st.session_state={}
        st.switch_page("admin_login.py")

st.subheader("Complaints Received") 

m = st.markdown("""
            <style>
            div.stButton > button:first-child {
            background-color: #003FDD;
            color: white;
            width: 200px;
            margin-top: 20px;
            text-alingn: center;
            }
            </style>""", unsafe_allow_html=True)

m = st.markdown("""
            <style>
            summary {
            background-color: #003FDD;
            font-size: 16px;
            font-weight: bold;
            border-radius: 20px;
            color: white;
            width: 200px;
            text-alingn: center;
            }
                .stVerticalBlockBorderWrapper{
                    border: solid 2px #003FDD;
                }
            .st-emotion-cache-1puwf6r p {
            font-size: 18px;
            color: white;
            }
            </style>""", unsafe_allow_html=True)

cred = credentials.Certificate("jaldisihdb-firebase-adminsdk-m5jux-16bbbd3d4b.json")
try:
    firebase_admin.initialize_app(cred, {
        'databaseURL': 'https://jaldisihdb-default-rtdb.firebaseio.com/'
    })
except:
    pass
status_ref = db.reference('tickets')
reponse = status_ref.get()

try:
    index = 0
    for key in reponse:
        md = reponse[key]
        if md["status"]!="Completed":
            index+=1
            # with st.container(border=True):
            #     a,b,c = st.columns([0.21,0.59,0.2])
            #     with a:
            #         st.subheader(f"{md['complaintType']}")
            #         st.write(f"Reported by {md['username']}")
            #         st.write(f"{md['timestamp']}")
            #     with b:
            #         st.subheader("Description:")
            #         st.write(f"{md['complaintDescription']}")
            #     with c:
            #         if st.button("View"):
            #             st.write("Viewed !")

            with st.container(border=True):
                st.subheader(f"#{key} - {md['complaintType']}")
                with st.expander("Full Details of Report"):
                    a,b = st.columns([0.3,0.7])
                    nig = st.container()
                with a:
                    from streamlit_folium import st_folium
                    import folium
                    latitude = md["latitude"]
                    longitude = md["longitude"]

                    m = folium.Map(location=[latitude, longitude], zoom_start=14)
                    folium.Marker([latitude, longitude], popup="Center").add_to(m)
                    st.write(" ")
                    st_folium(m, width=350, height=350,key=key)

                with b:
                    st.markdown('''
                                <style>b{font-size: 25px} span{padding-left: 10px;font-size: 25px}</style><br>
                        <div style="display: flex; flex-direction: column; gap: 10px;">
                            <div>
                                <b>Reported By:</b> <span>'''+md['username']+'''</span>
                            </div>
                            <div>
                                <b>Time Stamp:</b> <span>'''+md['timestamp']+'''</span>
                            </div>
                            <div>
                                <b>Description:</b> <span>'''+md['complaintDescription']+'''</span>
                            </div>
                        </div>
                ''',unsafe_allow_html=True)
                    d,f,g,h = st.columns(4)
                    statref = db.reference(f"tickets/{key}/status")
                    st.warning(f"Ticket Status: {statref.get()}..")
                    space = st.container()
                    
                    import time
                    with d:
                        import random as r
                        if st.button("Fetch GIS info",key=key):
                            with nig:
                                with st.spinner("Connecting to GIS Server.."):
                                    time.sleep(2)
                                with st.spinner("Formatting Response..."):
                                    status_ref2 = db.reference('Request')
                                    #1- pipe 2- pumphouses 3- plumbers 4- shop
                                    status_ref2.set(f"{md['latitude']},{md['longitude']},1")
                                    resp_ref = db.reference('Response')
                                    response = resp_ref.get()
                                    while (resp_ref.get()==response):
                                        pass
                                    new_resp = resp_ref.get()
                                    #st.write(new_resp)
                                    resp_ref.delete()
                                st.header("Pipelines running nearby")
                                st.markdown("<hr style='border: solid 2px #003FDB;margin:0em;'>",unsafe_allow_html=True)
                                q,w,e,r,t = st.columns(5)
                                cols = [q,w,e,r,t]
                                i=0
                                for pipe in new_resp:
                                    with cols[i]:
                                        with st.container(border=True):
                                            st.subheader(pipe["remarks"])
                                            st.markdown("<hr style='border: solid 1px #003FDB;margin:0em;'>",unsafe_allow_html=True)
                                            st.write(f'Type: {pipe["type"]}')
                                            st.write(f'Structure: {pipe["structure"]}')
                                            st.write(f'Diameter: {pipe["diameter"]}')
                                            st.write(f'Maintenance Due: {pipe["last_maint"]}')
                                            st.write(f'Lifetime: {pipe["TTL"]}')
                                    i+=1  
                                
                    with f:
                        if st.button("Assign Service Men", key=key*4, disabled=False):
                            data_add = key
                            st.session_state["data_add"] = key
                            st.switch_page("pages/plumber_assign.py")
                                
                        # if st.button("Check Inventory Status", key=key*2):
                        #         st.write("Fetching inventory status...")
                    with g:
                        if st.button("Payment Info", key=key*3,disabled=False):
                            data_add = key
                            st.session_state["data_add"] = key
                            st.switch_page("pages/payments.py")
                    
                    with h:
                        if st.button("Mark as Complete",key[::-1],disabled=False):
                            ref = db.reference(f"tickets/{key}/status")
                            ref.set("Completed")
    if index==0:
        st.success("No complaints")
except Exception as e:
    st.write(e)


